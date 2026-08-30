package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;

@Slf4j
@Singleton
public class FishDraynor extends PriorityTask {

    private static final int FISHING_RADIUS = 12;

    @Inject
    private FishingConfig config;

    @Inject
    private FishingPlugin plugin;

    @Override
    public boolean validate() {
        return ctx.players().local().isInArea(FishingLocation.DRAYNOR_VILLAGE.getLocation(), FISHING_RADIUS) &&
                ctx.players().local().isIdle() &&
                !ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        if (!ctx.inventory().hasItem(ItemID.NET)) {
            plugin.pauseScript("No small fishing net in the inventory");
            return 0;
        }

        NpcEntity spot = ctx.npcs().withId(FishingLocation.DRAYNOR_VILLAGE.getSpotId()).nearest().orElse(null);
        if (spot == null) {
            plugin.setTargetSpot(null);
            log.info("No spot found.");
            return RandomService.between(1200, 1800);
        }

        plugin.setTargetSpot(spot);
        if(config.useMouse()) {
            ctx.getMouse().move(spot.raw());
        }

        if (spot.interact("Small Net") && !SleepService.sleepUntil(() -> !ctx.players().local().isIdle(), 5000)) {
            log.info("Clicked the fishing spot but never started fishing.");
        }
        return RandomService.between(1200, 1800);
    }

    @Override
    public String status() {
        return "Fishing (Draynor Village)";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
