package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.query.groundobject.GroundObjectEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

@Slf4j
@Singleton
public class BuryBonesTask extends AbstractTask {

    @Inject
    private CombatConfig config;

    @Inject
    private ScriptContext scriptContext;

    @Override
    public boolean validate() {
        if (!config.buryBones()
                || scriptContext.getTarget() != null
                || ctx.players().local().raw().isInteracting()) {
            return false;
        }

        return ctx.inventory().withAction("Bury").isPresent()
                || (!ctx.inventory().isFull() && groundBones() != null);
    }

    @Override
    public int execute() {
        InventoryEntity bones = ctx.inventory().withAction("Bury").first();
        if (bones != null) {
            bones.interact("Bury");
            // Burying blocks other actions for ~2 ticks
            return RandomService.randomGaussian(1500, 200);
        }

        GroundObjectEntity ground = groundBones();
        if (ground == null) {
            return RandomService.between(200, 500);
        }

        int id = ground.getId();
        WorldPoint location = ground.raw().getLocation();
        ground.take();
        SleepService.sleepUntil(() -> ctx.groundItems().withId(id).at(location).isEmpty(),
                RandomService.between(4000, 7000));
        return RandomService.between(300, 800);
    }

    @Override
    public String status() {
        return "Burying bones";
    }

    private GroundObjectEntity groundBones() {
        return ctx.groundItems()
                .filter(g -> g.raw().isOwnedByLocalPlayer())
                .nameContains("bones")
                .reachable()
                .nearest();
    }
}
