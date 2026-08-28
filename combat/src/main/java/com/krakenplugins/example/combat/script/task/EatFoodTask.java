package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class EatFoodTask extends AbstractTask {

    @Inject
    private CombatConfig config;

    @Inject
    private ScriptContext scriptContext;

    @Override
    public boolean validate() {
        return ctx.players().local().getHealthPercentage() <= scriptContext.getEatThreshold()
                && ctx.inventory().hasFood();
    }

    @Override
    public int execute() {
        InventoryEntity food = ctx.inventory().food().first();
        if (food == null) {
            return RandomService.between(300, 600);
        }

        food.interact("Eat");
        scriptContext.rollEatThreshold(config.eatAt());

        // Eating blocks the next bite for ~3 ticks
        return RandomService.randomGaussian(1900, 200);
    }

    @Override
    public String status() {
        return "Eating";
    }
}
