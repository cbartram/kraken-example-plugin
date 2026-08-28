package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.combat.script.CombatScript;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReturnToFightAreaTask extends AbstractTask {

    @Inject
    private ScriptContext scriptContext;

    @Inject
    private Walker walker;

    @Override
    public boolean validate() {
        return scriptContext.getTarget() == null
                && ctx.inventory().hasFood()
                && scriptContext.getFightAnchor() != null
                && !ctx.players().local().isInArea(scriptContext.getFightAnchor(), CombatScript.FIGHT_AREA_RADIUS);
    }

    @Override
    public int execute() {
        ctx.players().local().activateRun();
        WalkResult result = walker.walkTo(scriptContext.getFightAnchor(), 5);
        if (!result.isSuccess()) {
            log.warn("Walk to fight area failed: {}", result);
            return RandomService.between(1500, 3000);
        }
        return RandomService.between(600, 1200);
    }

    @Override
    public String status() {
        return "Walking to fight area";
    }
}
