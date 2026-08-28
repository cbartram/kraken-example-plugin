package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.CombatScript;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RestockTask extends AbstractTask {

    @Inject
    private CombatConfig config;

    @Inject
    private ScriptContext scriptContext;

    @Inject
    private Walker walker;

    @Inject
    private BankService bankService;

    @Override
    public boolean validate() {
        return !ctx.inventory().hasFood();
    }

    @Override
    public int execute() {
        scriptContext.setTarget(null);

        if (!ctx.players().local().isInArea(CombatScript.VARROCK_EAST_BANK, 8)) {
            ctx.players().local().activateRun();
            WalkResult result = walker.walkTo(CombatScript.VARROCK_EAST_BANK, 4);
            if (!result.isSuccess()) {
                log.warn("Walk to bank failed: {}", result);
                return RandomService.between(1500, 3000);
            }
        }

        if (!bankService.isOpen()) {
            GameObjectEntity booth = ctx.gameObjects().withAction("Bank").reachable().nearest();
            if (booth == null) {
                log.warn("No bank object found near {}", CombatScript.VARROCK_EAST_BANK);
                return RandomService.between(1200, 2400);
            }

            booth.interact("Bank");
            if (!SleepService.sleepUntil(bankService::isOpen, RandomService.between(6000, 9000))) {
                return RandomService.between(600, 1200);
            }
        }

        if (!ctx.inventory().isEmpty()) {
            bankService.depositAll();
            SleepService.sleep(400, 900);
        }

        BankEntity food = ctx.bank().withName(config.foodName()).first();
        if (food == null) {
            scriptContext.setHaltReason("Out of " + config.foodName() + " - script halted");
            bankService.close();
            return RandomService.between(600, 1200);
        }

        // Leave room for drops when looting or burying, otherwise fill with as much food as configured
        int reserve = (config.buryBones() || !config.lootIds().isBlank()) ? 4 : 0;
        int amount = Math.max(1, Math.min(config.foodAmount(), ctx.inventory().freeSpace() - reserve));
        food.withdraw(amount);
        SleepService.sleepUntil(() -> ctx.inventory().hasFood(), 4000);

        SleepService.sleep(400, 1000);
        bankService.close();
        return RandomService.between(600, 1200);
    }

    @Override
    public String status() {
        return "Restocking food";
    }
}
