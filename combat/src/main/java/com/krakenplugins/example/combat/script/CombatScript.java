package com.krakenplugins.example.combat.script;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.kraken.api.service.tile.TileService;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.task.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class CombatScript extends Script {

    public static final WorldPoint VARROCK_EAST_BANK = new WorldPoint(3253, 3421, 0);
    public static final int FIGHT_AREA_RADIUS = 15;

    private final List<Task> tasks;
    private final ScriptContext scriptContext;
    private final CombatConfig config;
    private final TileService tileService;
    private final Context ctx;

    @Inject
    public CombatScript(CombatConfig config, Context ctx, ScriptContext scriptContext, TileService tileService,
                        EatFoodTask eatFoodTask, RestockTask restockTask, LootTask lootTask,
                        BuryBonesTask buryBonesTask, ReturnToFightAreaTask returnToFightAreaTask,
                        FindTargetTask findTargetTask, CombatMonitorTask combatMonitorTask) {
        this.config = config;
        this.ctx = ctx;
        this.scriptContext = scriptContext;
        this.tileService = tileService;
        // Order matters: only the first task whose validate() passes runs each loop, so
        // survival (eating, restocking) is checked before combat and combat before idling.
        this.tasks = List.of(
                eatFoodTask,
                restockTask,
                lootTask,
                buryBonesTask,
                returnToFightAreaTask,
                findTargetTask,
                combatMonitorTask
        );
    }

    @Override
    public void onStart() {
        scriptContext.setStartTimeMillis(System.currentTimeMillis());
        scriptContext.setHaltReason(null);
        scriptContext.setTarget(null);
        scriptContext.setFightAnchor(resolveFightAnchor());
        scriptContext.rollEatThreshold(config.eatAt());
        scriptContext.setStatus("Starting");

        if (config.npcTarget().isBlank()) {
            log.warn("No NPC target configured; set one in the plugin config");
        }
    }

    /**
     * The configured "x,y,plane" fight location, or wherever the script is started when the
     * config is blank or unparseable.
     */
    private WorldPoint resolveFightAnchor() {
        String value = config.fightLocation().trim();
        if (!value.isBlank()) {
            String[] parts = value.split(",");
            if (parts.length == 3) {
                try {
                    return new WorldPoint(
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            log.warn("Could not parse fight location '{}', expected \"x,y,plane\"; using current position", value);
        }
        return ctx.players().local().location();
    }

    @Override
    public int loop() {
        if (scriptContext.getHaltReason() != null) {
            scriptContext.setStatus(scriptContext.getHaltReason());
            return RandomService.between(4000, 7000);
        }

        if (config.highlightReachableTiles()) {
            // Swap in a new list rather than mutating the one the overlay is iterating
            scriptContext.setReachableTiles(new ArrayList<>(tileService.getReachableTilesFromTile(
                    ctx.players().local().location(),
                    config.reachableTileDist(),
                    false
            ).keySet()));
        }

        for (Task task : tasks) {
            try {
                if (task.validate()) {
                    scriptContext.setStatus(task.status());
                    return task.execute();
                }
            } catch (Throwable t) {
                log.error("Task {} failed", task.getClass().getSimpleName(), t);
                return RandomService.between(600, 1200);
            }
        }

        scriptContext.setStatus("Waiting");
        return RandomService.between(200, 600);
    }

    @Override
    public void onStop() {
        scriptContext.setTarget(null);
        scriptContext.setStatus("Stopped");
    }
}
