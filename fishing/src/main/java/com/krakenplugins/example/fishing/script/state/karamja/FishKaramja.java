package com.krakenplugins.example.fishing.script.state.karamja;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.pathfinding.GlobalPathfinder;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;

@Slf4j
@Singleton
public class FishKaramja extends PriorityTask {

    private static final int FISHING_RADIUS = 12;

    @Inject
    private FishingPlugin plugin;

    @Inject
    private FishingConfig config;

    @Inject
    private GlobalPathfinder globalPathfinder;

    @Override
    public boolean validate() {
        return ctx.players().local().isInArea(FishingLocation.KARAMJA.getLocation(), FISHING_RADIUS) &&
                ctx.players().local().isIdle() &&
                !ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        if (!ctx.inventory().hasItem(ItemID.HARPOON) && !ctx.inventory().hasItem(ItemID.LOBSTER_POT)) {
            plugin.pauseScript("No harpoon or lobster pot in the inventory");
            return 0;
        }

        NpcEntity spot = ctx.npcs().withId(FishingLocation.KARAMJA.getSpotId()).nearest().orElse(null);
        if (spot == null) {
            plugin.setTargetSpot(null);
            log.info("No spot found.");
            return RandomService.between(1200, 1800);
        }

        // The walk is over, so drop the route the pathfinder overlay is still drawing.
        globalPathfinder.clearLastResult();
        plugin.setTargetSpot(spot);

        if (config.useMouse()) {
            ctx.getMouse().move(spot.raw());
        }

        log.info("Finding new fishing spot...");
        if (spot.interact(config.fishingMethod().getInteractionName())
                && !SleepService.sleepUntil(() -> !ctx.players().local().isIdle(), 5000)) {
            log.info("Clicked the fishing spot but never started fishing.");
        }
        return RandomService.between(1200, 1800);
    }

    @Override
    public String status() {
        return "Fishing (Karamja)";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
