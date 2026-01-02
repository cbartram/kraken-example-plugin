package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.ui.dialogue.DialogueService;
import com.kraken.api.service.ui.processing.ProcessingService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class BurnLogsTask extends AbstractTask {

    @Inject
    private FiremakingConfig config;

    @Inject
    private BankService bankService;

    @Inject
    private FiremakingPlugin plugin;

    @Inject
    private ProcessingService processingService;

    @Inject
    private DialogueService dialogueService;

    private static final int BURNING_ANIM = 10572;

    @Override
    public boolean validate() {
        log.info("Has logs: {}, and tinderbox: {}", ctx.inventory().hasItem(config.logName()), ctx.inventory().hasItem(590));
        return ctx.inventory().hasItem(config.logName())
                && ctx.inventory().hasItem(590) // 590 is Tinderbox ID
                && !bankService.isOpen()
                && ctx.players().local().isIdle();
    }

    // Priority for this method goes:
    // If forest fire exists, use that
    // If normal fire exists, turn it into a foresters fire and use that
    // Light a new fire and turn it into a foresters fire.

    // TODO 2 issues
    //  - When a level up occurs players animation when doing a bonfire doesn't change and thus we get stuck in the execute() loop
    //  - When we run out of logs we just stand there (execute shouldn't run) but does?
    @Override
    public int execute() {
        InventoryEntity tinderbox = ctx.inventory().withName("Tinderbox").first();
        InventoryEntity randomLog = ctx.inventory().withName(config.logName()).random();

        // Get the nearest fire rather than a random one to prevent long runs
        GameObjectEntity fire = ctx.gameObjects().withId(26185).nearest();
        GameObjectEntity foresterFire = ctx.gameObjects().withId(49927).nearest();

        // Level up continue widget: Id	15269891, text= Click here to continue, group = 233 child = 3
        // 1. Handle Animation Delays
        if (ctx.players().local().raw().getAnimation() == BURNING_ANIM) {
            if(dialogueService.isDialoguePresent()) {
                log.info("Level up message present");
                dialogueService.continueDialogue();
            }

            if(ctx.getClient().getTickCount() - plugin.getLastFiremakingXpDropTick() > 12) {
                log.info("It's been more than 12 ticks since the last xp drop, restarting log burn bonfire");
//                if (config.useMouse()) {
//                    ctx.getMouse().move(randomLog.raw());
//                }
//
//                log.info("Using logs on fire...");
//                randomLog.useOn(fire.raw());
//
//                // Wait for processing interface or animation
//                SleepService.sleepUntil(() -> processingService.isOpen(), RandomService.between(4000, 6000));
//
//                log.info("Processing {}...", config.logName());
//                processingService.process(config.logName());
            }

            log.info("Sleeping a tick due to bonfire animation");
            return 600;
        }


        // Always prioritize forester fire over starting a new forester fire
        if(foresterFire != null) {
            plugin.setTargetFire(foresterFire.raw());
            if (config.useMouse()) {
                ctx.getMouse().move(randomLog.raw());
            }

            log.info("Using logs on existing foresters fire...");
            randomLog.useOn(foresterFire.raw());

            // Wait for processing interface or animation
            SleepService.sleepUntil(() -> processingService.isOpen(), RandomService.between(4000, 6000));

            log.info("Processing {}...", config.logName());
            processingService.process(config.logName());
            return 600;
        }

        // 2. If existing fire is present (and close), add logs to it, turning it into a foresters fire
        if (fire != null && randomLog != null && fire.raw().getWorldLocation().distanceTo(ctx.players().local().raw().getWorldLocation()) < 10) {
            plugin.setTargetFire(fire.raw());
            if (config.useMouse()) {
                ctx.getMouse().move(randomLog.raw());
            }

            log.info("Using logs on fire...");
            randomLog.useOn(fire.raw());

            // Wait for processing interface or animation
            SleepService.sleepUntil(() -> processingService.isOpen(), RandomService.between(4000, 6000));

            log.info("Processing {}...", config.logName());
            processingService.process(config.logName());
            return 600;
        }

        // 3. Select a point not in the bank area but within 4 tiles of a random bank tile
        Set<WorldPoint> bankTiles = plugin.getBankLocation().getTiles();
        WorldPoint myLoc = ctx.players().local().raw().getWorldLocation();

        // If we are currently standing IN the bank, we must move out
        if (bankTiles.contains(myLoc)) {
            if (ctx.players().local().isMoving()) {
                return 600;
            }

            WorldPoint targetSpot = findSafeSpot(bankTiles);
            if (targetSpot != null) {
                log.info("Walking to safe firemaking spot: {}", targetSpot);
                ctx.getService(MovementService.class).moveTo(targetSpot);
                SleepService.sleepUntil(() -> ctx.players().local().isMoving(), 1200);
                return 1200;
            } else {
                log.warn("Could not find a valid firemaking spot nearby.");
                return 1000;
            }
        }

        // 4. Light fire
        // We are now outside the bank, and no valid fire exists nearby.
        if (tinderbox != null && randomLog != null) {
            log.info("Lighting new fire...");
            tinderbox.useOn(randomLog.raw());

            // Wait for the animation to start so we don't spam click
            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() != -1, RandomService.between(1200, 1800));
            return 600;
        }

        return 600;
    }

    /**
     * Helper to find a tile that is NOT in the bank, but close to it.
     */
    private WorldPoint findSafeSpot(Set<WorldPoint> bankTiles) {
        if (bankTiles.isEmpty()) return null;

        // Convert set to list to pick a random anchor point
        List<WorldPoint> tilesList = new ArrayList<>(bankTiles);

        // Try up to 10 times to find a valid spot to avoid infinite loops
        for (int i = 0; i < 10; i++) {
            WorldPoint randomBankTile = tilesList.get(RandomService.between(0, tilesList.size()));

            int dx = RandomService.between(-4, 4);
            int dy = RandomService.between(-4, 4);

            WorldPoint candidate = randomBankTile.dx(dx).dy(dy);

            if (!bankTiles.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public String status() {
        return "Burning Logs";
    }
}