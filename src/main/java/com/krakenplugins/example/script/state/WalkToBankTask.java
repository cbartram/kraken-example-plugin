package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.MiningPlugin;
import com.krakenplugins.example.script.AbstractTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.MiningPlugin.BANK_INTERMEDIATE_LOCATION;
import static com.krakenplugins.example.MiningPlugin.BANK_LOCATION;

@Slf4j
@Singleton
public class WalkToBankTask extends AbstractTask {

    @Inject
    private LocalPathfinder pathfinder;

    @Inject
    private MiningPlugin plugin;

    @Setter
    @Getter
    private boolean arrivedAtIntermediatePoint = false;

    @Override
    public boolean validate() {
        boolean playerNotInBank = !ctx.players().local().isInArea(BANK_LOCATION, 3);
        return ctx.inventory().isFull()
                && playerNotInBank
                && ctx.players().local().isIdle();
    }

    @Override
    public int execute() {
        plugin.setTargetRock(null);
        Player localPlayer = ctx.players().local().raw();

        if (arrivedAtIntermediatePoint) {
            List<WorldPoint> path = pathfinder.findSparsePath(localPlayer.getWorldLocation(), BANK_LOCATION);
            if (!path.isEmpty()) {
                log.info("Calculated path of length: {} to bank point", path.size());
                plugin.getCurrentPath().addAll(path);
                return 300;
            }

            log.warn("Could not find path to bank from intermediate point. Waiting before retry.");
            return 1000;
        }

        // We haven't reached the intermediate point yet, check if we have now arrived.
        if (localPlayer.getWorldLocation().distanceTo(BANK_INTERMEDIATE_LOCATION) <= 4) {
            log.info("Arrived at intermediate point.");
            arrivedAtIntermediatePoint = true;
            return 300; // Re-run task on next tick to plan path to bank
        }

        // Still not at the intermediate point, calculate a path to it.
        List<WorldPoint> path = pathfinder.findSparsePath(localPlayer.getWorldLocation(), BANK_INTERMEDIATE_LOCATION);
        if (!path.isEmpty()) {
            log.info("Calculated path of length: {} to bank intermediate point", path.size());
            plugin.getCurrentPath().addAll(path);
        } else {
            log.warn("Could not find path to intermediate location.");
        }
        return 300;
    }

    @Override
    public String status() {
        return "Pathing to bank";
    }
}
