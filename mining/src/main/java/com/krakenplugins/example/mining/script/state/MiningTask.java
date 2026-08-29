package com.krakenplugins.example.mining.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.pathfinding.GlobalPathfinder;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.mining.MiningPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.mining.MiningPlugin.DEPLETED_ROCKS;
import static com.krakenplugins.example.mining.MiningPlugin.IRON_ROCKS;

@Slf4j
@Singleton
public class MiningTask extends AbstractTask {

    // How long a click has to produce a walk or a swing before it is treated as lost.
    private static final long INTERACTION_TIMEOUT_MS = 3000;

    @Inject
    private MiningPlugin plugin;

    @Inject
    private GlobalPathfinder pathfinder;

    @Override
    public boolean validate() {
        return !ctx.inventory().isFull() && ctx.players().local().isInArea(plugin.getMiningArea());
    }

    @Override
    public int execute() {
        pathfinder.clearLastResult();

        GameObjectEntity rock = findRock();
        if (rock == null) {
            plugin.setTargetRock(null);
            return 650;
        }

        plugin.moveMouseTo(rock.raw());
        if (!rock.interact("Mine")) {
            log.info("Mine action was not available on the rock at {}", rock.raw().getWorldLocation());
            plugin.setTargetRock(null);
            return 600;
        }
        plugin.setTargetRock(rock.raw());

        // The click is only acted on next tick, so wait for the walk or the swing to start before
        // reading "idle" as "this rock is spent".
        if (!SleepService.sleepUntil(() -> !ctx.players().local().isIdle(), INTERACTION_TIMEOUT_MS)) {
            log.info("Mine click never took effect, picking another rock");
            plugin.setTargetRock(null);
            return 0;
        }

        // Going idle again means the rock is mined out, or that something interrupted us. Either way
        // the next loop picks a fresh rock.
        SleepService.sleepUntilIdle();
        plugin.setTargetRock(null);
        return 0;
    }

    /**
     * Returns the closest iron rock that still holds ore, or null while they are all respawning.
     * Spent rocks are what separate a respawn wait from standing somewhere with no iron at all,
     * which no amount of retrying will fix.
     *
     * @return The rock to mine next, or null if there is nothing to mine right now.
     */
    private GameObjectEntity findRock() {
        GameObjectEntity rock = ctx.gameObjects()
                .filter(object -> IRON_ROCKS.contains(object.getId()) && object.isInArea(plugin.getMiningArea()))
                .nearest();

        if (rock == null) {
            boolean respawning = !ctx.gameObjects()
                    .filter(object -> DEPLETED_ROCKS.contains(object.getId()) && object.isInArea(plugin.getMiningArea()))
                    .isEmpty();

            if (respawning) {
                log.info("Every iron rock is mined out, waiting on a respawn");
            } else {
                plugin.halt("No iron rocks in the mining area");
            }
        }

        return rock;
    }

    @Override
    public String status() {
        return "Mining Ore";
    }
}
