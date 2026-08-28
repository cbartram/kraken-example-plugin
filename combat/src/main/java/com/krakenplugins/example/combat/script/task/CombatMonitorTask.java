package com.krakenplugins.example.combat.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

@Slf4j
@Singleton
public class CombatMonitorTask extends AbstractTask {

    private static final int IDLE_LOOPS_BEFORE_RESET = 4;

    @Inject
    private ScriptContext scriptContext;

    @Inject
    private MovementService movementService;

    private int idleLoops;

    @Override
    public boolean validate() {
        return scriptContext.getTarget() != null;
    }

    @Override
    public int execute() {
        NPC target = scriptContext.getTarget();

        if (target.isDead()) {
            scriptContext.setTarget(null);
            idleLoops = 0;
            // Linger through the death animation so drops hit the ground before looting starts
            return RandomService.randomGaussian(2000, 350);
        }

        // getInteracting() resolves an actor index and must run on the client thread
        Actor interacting = ctx.runOnClientThread(() -> ctx.players().local().raw().getInteracting());
        if (interacting instanceof NPC && ((NPC) interacting).getIndex() != target.getIndex()) {
            // Something else is attacking us; fight back instead of splitting attention
            scriptContext.setTarget((NPC) interacting);
            idleLoops = 0;
            return RandomService.between(300, 700);
        }

        WorldPoint safespot = scriptContext.getSafespot();
        if (safespot != null && !ctx.players().local().location().equals(safespot)) {
            movementService.moveTo(safespot);
            return RandomService.between(600, 1200);
        }

        // Target alive but neither of us is doing anything: the attack click likely failed or
        // the NPC despawned, so drop the target and let FindTargetTask re-acquire
        if (interacting == null && !ctx.players().local().isMoving()) {
            if (++idleLoops >= IDLE_LOOPS_BEFORE_RESET) {
                idleLoops = 0;
                scriptContext.setTarget(null);
            }
        } else {
            idleLoops = 0;
        }

        return RandomService.between(300, 700);
    }

    @Override
    public String status() {
        return "Fighting";
    }
}
