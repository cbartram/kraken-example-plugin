package com.krakenplugins.example.fishing.script.state.barbarian;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.ui.processing.ProcessingService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

@Slf4j
@Singleton
public class CookFish extends PriorityTask {

    /** Doubles as how far the fire may be, so the script does not walk across the scene to one. */
    private static final int FIRE_RADIUS = 15;

    /** Quantity handed to the "How many?" interface, so one click cooks the whole inventory. */
    private static final int COOK_ALL = 28;

    /**
     * A full inventory cooks in roughly 28 fish x 4 ticks. The wait resolves as soon as the raw fish
     * run out, so this only caps an interrupted cook.
     */
    private static final long COOK_TIMEOUT_MS = 75_000;

    private static final long IDLE_RESET_MS = 2000;
    private static final double BASE_REACTION_CHANCE = 0.02;

    private static final int LOW_TICK_THRESHOLD = 1000;
    private static final int MEDIUM_TICK_THRESHOLD = 3000;
    private static final int HIGH_TICK_THRESHOLD = 6000;

    private static final double INCREMENT_LOW = 0.04;
    private static final double INCREMENT_MEDIUM = 0.025;
    private static final double INCREMENT_HIGH = 0.015;
    private static final double INCREMENT_VERY_HIGH = 0.01;

    @Inject
    private FishingConfig config;

    @Inject
    private FishingPlugin plugin;

    @Inject
    private ProcessingService processingService;

    private int idleTicks = 0;
    private long lastLoopTime = 0;

    @Override
    public boolean validate() {
        return config.barbVillageCook() &&
                ctx.inventory().isFull() &&
                (ctx.inventory().hasItem(ItemID.RAW_TROUT) || ctx.inventory().hasItem(ItemID.RAW_SALMON)) &&
                ctx.players().local().isInArea(FishingLocation.BARBARIAN_VILLAGE.getLocation(), FIRE_RADIUS) &&
                ctx.players().local().isIdle();
    }

    @Override
    public int execute() {
        if (processingService.isOpen()) {
            idleTicks = 0;
            return cook();
        }

        long now = System.currentTimeMillis();
        if (now - lastLoopTime > IDLE_RESET_MS) {
            idleTicks = 0;
        }
        lastLoopTime = now;

        double reactionIncrement = getReactionIncrement();
        double reactionChance = BASE_REACTION_CHANCE + (idleTicks * reactionIncrement);
        reactionChance = Math.min(1.0, reactionChance);
        if (Math.random() > reactionChance) {
            log.info("Missed reaction window before cooking, increasing reaction chance by {} next tick. Current chance = {}", reactionIncrement, reactionChance);
            idleTicks++;
            return RandomService.between(400, 600);
        }

        // Barbarian Village's permanent fire, or one the player lit. Both come out of a single scene pass.
        GameObjectEntity fire = ctx.gameObjects()
                .filter(o -> o.getId() == ObjectID.FIRE_COOK || o.getId() == ObjectID.FIRE)
                .within(FIRE_RADIUS)
                .nearest();

        if (fire != null && fire.interact("Cook")) {
            idleTicks = 0;
            if (!SleepService.sleepUntilTrue(processingService::isOpen, 400, 5000)) {
                log.warn("Clicked the fire but the cooking interface never opened.");
            }
        }

        return 0;
    }

    /**
     * Cooks a full inventory of whichever raw fish is held. {@code process} matches on the id of the
     * product, which is the only thing the interface lists.
     */
    private int cook() {
        final int rawId = ctx.inventory().hasItem(ItemID.RAW_TROUT) ? ItemID.RAW_TROUT : ItemID.RAW_SALMON;
        final int cookedId = rawId == ItemID.RAW_TROUT ? ItemID.TROUT : ItemID.SALMON;

        processingService.setAmount(COOK_ALL);

        if (!processingService.process("Cook", cookedId)) {
            // Retrying cannot change what the interface lists, so stop instead of spinning on it.
            plugin.pauseScript("Cooking interface does not offer item id " + cookedId);
            return 0;
        }

        // The interface cooks the whole inventory and leaves a one tick idle gap between each fish, so
        // wait on the raw fish running out. Waiting on a single idle tick would read a gap as finished
        // and click the fire again mid-cook.
        if (!SleepService.sleepUntilTrue(() -> !ctx.inventory().hasItem(rawId), 600, COOK_TIMEOUT_MS)) {
            log.warn("Still holding raw fish after {}ms, cooking was interrupted.", COOK_TIMEOUT_MS);
        }
        return 600;
    }

    @Override
    public String status() {
        return "Cooking Fish";
    }

    @Override
    public int getPriority() {
        // Ahead of DropFish, which validates on a full inventory alone and would otherwise throw the
        // raw fish away before cooking is ever considered.
        return -2;
    }

    private double getReactionIncrement() {
        int tickCount = ctx.getClient().getTickCount();
        if (tickCount < LOW_TICK_THRESHOLD) {
            return INCREMENT_LOW;
        }
        if (tickCount < MEDIUM_TICK_THRESHOLD) {
            return INCREMENT_MEDIUM;
        }
        if (tickCount < HIGH_TICK_THRESHOLD) {
            return INCREMENT_HIGH;
        }
        return INCREMENT_VERY_HIGH;
    }
}
