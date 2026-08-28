package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.groundobject.GroundObjectEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class LootTask extends AbstractTask {

    @Inject
    private CombatConfig config;

    @Inject
    private ScriptContext scriptContext;

    @Override
    public boolean validate() {
        return scriptContext.getTarget() == null
                && !ctx.players().local().raw().isInteracting()
                && !ctx.inventory().isFull()
                && findLoot() != null;
    }

    @Override
    public int execute() {
        GroundObjectEntity loot = findLoot();
        if (loot == null) {
            return RandomService.between(200, 500);
        }

        int id = loot.getId();
        WorldPoint location = loot.raw().getLocation();
        loot.take();
        SleepService.sleepUntil(() -> ctx.groundItems().withId(id).at(location).isEmpty(),
                RandomService.between(4000, 7000));
        return RandomService.between(300, 800);
    }

    @Override
    public String status() {
        return "Looting";
    }

    private GroundObjectEntity findLoot() {
        GroundObjectEntity valuable = ctx.groundItems()
                .filter(g -> g.raw().isOwnedByLocalPlayer())
                .stackValueAbove(config.lootValueThreshold())
                .reachable()
                .nearest();


        if (valuable != null) {
            return valuable;
        }

        Set<Integer> ids = lootIds();
        if (ids.isEmpty()) {
            return null;
        }
        return ctx.groundItems()
                .filter(g -> g.raw().isOwnedByLocalPlayer())
                .filter(g -> ids.contains(g.getId()))
                .reachable()
                .nearest();
    }

    private Set<Integer> lootIds() {
        return Arrays.stream(config.lootIds().split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }
}
