package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FishBarbarianVillage extends AbstractTask {

    @Inject
    private FishingPlugin plugin;

    @Inject
    private FishingConfig config;

    @Override
    public boolean validate() {
        // Rod and feathers
        if (!ctx.inventory().hasItem(309) || !ctx.inventory().hasItem(314)) return false;
        return ctx.players().local().isInArea(FishingLocation.BARBARIAN_VILLAGE.getLocation(), 15) &&
                ctx.players().local().isIdle() &&
                !ctx.inventory().isFull();
    }


    // TODO Logic that won't immediately click on the next spot as soon as this one dissapears (instead distribution on if this tick we click the next spot)
    // this simulates human: "oh I forgot to immediately click once my character went idle".
    @Override
    public int execute() {
        NpcEntity spot = ctx.npcs().withId(FishingLocation.BARBARIAN_VILLAGE.getSpotId()).nearest();
        if(spot != null) {
            plugin.setTargetSpot(spot);
            if(config.useMouse()) {
                ctx.getMouse().move(spot.raw());
            }
            if (spot.interact("Lure")) {
                SleepService.sleepUntil(() -> ctx.players().local().isMoving() || ctx.players().local().raw().getAnimation() != -1, 5000);
            }
        } else {
            log.info("No spot found.");
        }
        return 0;
    }

    @Override
    public String status() {
        return "Fishing (Barbarian Village)";
    }
}
