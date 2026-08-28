package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FindTargetTask extends AbstractTask {

    @Inject
    private CombatConfig config;

    @Inject
    private ScriptContext scriptContext;

    @Override
    public boolean validate() {
        return scriptContext.getTarget() == null
                && !config.npcTarget().isBlank()
                && !ctx.players().local().raw().isInteracting()
                && findTarget() != null;
    }

    @Override
    public int execute() {
        NpcEntity target = findTarget();
        if (target == null) {
            return RandomService.between(400, 900);
        }

        if (target.attack()) {
            scriptContext.setTarget(target.raw());
            SleepService.sleepUntil(() -> ctx.players().local().raw().isInteracting(),
                    RandomService.between(2400, 4200));
        }
        return RandomService.between(300, 700);
    }

    @Override
    public String status() {
        return "Finding target";
    }

    private NpcEntity findTarget() {
        return ctx.npcs().attackable().withName(config.npcTarget()).reachable().nearest();
    }
}
