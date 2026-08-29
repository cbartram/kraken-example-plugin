package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.dialogue.DialogueService;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.tile.TileService;
import com.kraken.api.service.ui.processing.ProcessingService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

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

    @Inject
    private MovementService movementService;

    @Inject
    private TileService tileService;

    /** Forester's Campfire. Has no gameval constant, the cache symbol is {@code forestry_fire}. */
    private static final int FORESTERS_CAMPFIRE = 49927;

    /** Fires further away than this are ignored so the script does not run across the whole scene. */
    private static final int MAX_FIRE_DISTANCE = 5;

    /** Failed light attempts on the same tile before giving up on it and picking another. */
    private static final int MAX_LIGHT_ATTEMPTS = 3;

    private int lightAttempts;

    @Override
    public boolean validate() {
        return ctx.inventory().hasItem(config.logName())
                && ctx.inventory().hasItem(ItemID.TINDERBOX)
                && !bankService.isOpen();
    }

    // Priority for this method goes:
    // If forest fire exists, use that
    // If normal fire exists, turn it into a foresters fire and use that
    // Light a new fire and turn it into a foresters fire.
    @Override
    public int execute() {
        if (dialogueService.isDialoguePresent()) {
            dialogueService.continueDialogue();
            return 600;
        }

        // Walking, lighting or burning. isBusy() lapses once an animation stops paying out xp, which is
        // what lets a bonfire that stopped consuming logs fall through and get restarted below.
        if (plugin.isBusy()) {
            return 600;
        }

        InventoryEntity randomLog = ctx.inventory().withName(config.logName()).random();
        if (randomLog == null) {
            return 600;
        }

        GameObjectEntity fire = nearestFire();
        if (fire != null) {
            startBonfire(fire, randomLog);
            return 600;
        }

        // No fire in reach, so we need to light one. Standing in the bank means moving out first.
        Set<WorldPoint> bankTiles = plugin.getBankLocation().getTiles();
        if (bankTiles.contains(ctx.players().local().location())) {
            return moveToSafeSpot(bankTiles);
        }

        InventoryEntity tinderbox = ctx.inventory().withId(ItemID.TINDERBOX).first();
        if (tinderbox == null) {
            return 600;
        }

        if (config.useMouse()) {
            ctx.getMouse().move(randomLog.raw());
        }

        tinderbox.useOn(randomLog.raw());
        plugin.markAction();

        // Wait for the animation to start so we don't spam click.
        if (SleepService.sleepUntil(() -> !ctx.players().local().isIdle(), RandomService.between(1200, 1800))) {
            lightAttempts = 0;
            return 600;
        }

        // Nothing happened, so this tile probably doesn't allow fires. Try somewhere else.
        if (++lightAttempts >= MAX_LIGHT_ATTEMPTS) {
            log.info("Could not light a fire here after {} attempts, moving elsewhere", lightAttempts);
            lightAttempts = 0;
            return moveToSafeSpot(plugin.getBankLocation().getTiles());
        }

        return 600;
    }

    /**
     * Finds the closest reachable fire to burn on, preferring a Forester's Campfire since it burns
     * faster. Both object types are collected in a single scene pass.
     */
    private GameObjectEntity nearestFire() {
        List<GameObjectEntity> fires = ctx.gameObjects()
                .filter(o -> o.getId() == FORESTERS_CAMPFIRE || o.getId() == ObjectID.FIRE)
                .within(MAX_FIRE_DISTANCE)
                .reachable()
                .sortByDistance()
                .list();

        GameObjectEntity nearestFire = null;
        for (GameObjectEntity candidate : fires) {
            if (candidate.getId() == FORESTERS_CAMPFIRE) {
                return candidate;
            }

            if (nearestFire == null) {
                nearestFire = candidate;
            }
        }

        return nearestFire;
    }

    private void startBonfire(GameObjectEntity fire, InventoryEntity logItem) {
        plugin.setTargetFire(fire.raw());
        if (config.useMouse()) {
            ctx.getMouse().move(logItem.raw());
        }

        logItem.useOn(fire.raw());
        plugin.markAction();

        if (!SleepService.sleepUntil(() -> processingService.isOpen(), RandomService.between(4000, 6000))) {
            log.debug("Burn amount dialogue never opened");
            return;
        }

        if (processingService.process("Burn", config.logName())) {
            plugin.markAction();
        }
    }

    private int moveToSafeSpot(Set<WorldPoint> bankTiles) {
        WorldPoint targetSpot = findSafeSpot(bankTiles);
        if (targetSpot == null) {
            log.warn("Could not find a valid firemaking spot nearby.");
            return 1000;
        }

        movementService.moveTo(targetSpot);
        plugin.markAction();
        SleepService.sleepUntil(() -> ctx.players().local().isMoving(), 1200);
        return 1200;
    }

    /**
     * Helper to find a reachable tile that is NOT in the bank, but close to it.
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

            if (!bankTiles.contains(candidate) && tileService.isTileReachable(candidate)) {
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
