package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.util.RandomService;
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

    private int idleTicks = 0;
    private long lastLoopTime = 0;

    @Override
    public boolean validate() {
        // Rod and feathers
        if (!ctx.inventory().hasItem(309) || !ctx.inventory().hasItem(314)) return false;

        return ctx.players().local().isInArea(FishingLocation.BARBARIAN_VILLAGE.getLocation(), 15) &&
                ctx.players().local().isIdle() &&
                !ctx.inventory().isFull();
    }

    // TODO Skip idle after a drop has occurred. Just do the click.
    @Override
    public int execute() {
        // If it's been more than 2000ms since the last execute call, we assume
        // this is a fresh "idle" session (we just finished fishing), so we reset the counter.
        if (System.currentTimeMillis() - lastLoopTime > 2000) {
            idleTicks = 0;
        }
        lastLoopTime = System.currentTimeMillis();

        // Check if we recently finished dropping (e.g., within 4 seconds)
        // If so, we are "Active" and should click immediately, bypassing the idle simulation.
        long timeSinceDrop = System.currentTimeMillis() - plugin.getLastDropTimestamp();
        boolean isActivePlayer = timeSinceDrop < 4000;

        if (!isActivePlayer) {

            // Calculate a chance to click this tick based on how long we've been waiting.
            // Tick 0: 5% chance (Fast reaction)
            // Tick 1: 10% chance
            // Tick 2: 15% chance
            // ...
            // Tick 20+: 100% chance (Guaranteed to click eventually)
            double reactionChance = 0.02 + (idleTicks * 0.02);

            if (Math.random() > reactionChance) {
                log.info("Missed reaction window, increasing reaction chance by 2% next tick. Current chance = {}", reactionChance);
                idleTicks++;
                return RandomService.between(400, 600);
            }
        }

        NpcEntity spot = ctx.npcs().withId(FishingLocation.BARBARIAN_VILLAGE.getSpotId()).nearest();
        if (spot != null) {
            plugin.setTargetSpot(spot);

            if (config.useMouse()) {
                ctx.getMouse().move(spot.raw());
            }

            if (spot.interact("Lure")) {
                // Successful click, reset idle ticks for the next time we become idle
                idleTicks = 0;
                SleepService.sleepUntil(() -> ctx.players().local().isMoving() || ctx.players().local().raw().getAnimation() != -1, 5000);
            }
        } else {
            log.info("No spot found.");
            idleTicks = 0;
        }

        return 0;
    }

    @Override
    public String status() {
        if (idleTicks > 0) {
            return "Idling (Reaction Delay)";
        }
        return "Fishing (Barbarian Village)";
    }
}