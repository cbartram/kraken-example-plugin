package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.script.FishingLocation;

public class FishDraynor extends AbstractTask {

    @Inject
    private FishingConfig config;

    @Inject
    private SleepService sleepService;

    @Override
    public boolean validate() {
        if (ctx.inventory().isFull()) return false;

        if (!ctx.inventory().hasItem(303)) return false;

        if (!ctx.players().local().isInArea(FishingLocation.DRAYNOR_VILLAGE.getLocation(), 12)) return false;

        // CRITICAL FIX: Better Idle Check
        // We are NOT valid to fish if we are currently interacting with the fishing spot
        // or if we are performing the fishing animation (Id: 621 for small net).
        // Adjust animation ID based on actual OSRS debug data.
        boolean isFishingAnim = ctx.players().local().raw().getAnimation() != -1; // -1 is usually idle
        boolean isInteracting = ctx.players().local().raw().getInteracting() != null
                && ctx.players().local().raw().getInteracting().getName().equalsIgnoreCase("Fishing spot");

        return !isFishingAnim && !isInteracting;
    }

    @Override
    public int execute() {
        NpcEntity spot = ctx.npcs().withId(FishingLocation.DRAYNOR_VILLAGE.getSpotId()).nearest();
        if(spot != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(spot.raw());
            }
            if (spot.interact("Small Net")) {
                sleepService.sleepUntil(() -> ctx.players().local().isMoving() || ctx.players().local().raw().getAnimation() != -1, 5000
                );
            }
        }
        return RandomService.between(1200, 1800);
    }

    @Override
    public String status() {
        return "Fishing (Draynor Village)";
    }
}
