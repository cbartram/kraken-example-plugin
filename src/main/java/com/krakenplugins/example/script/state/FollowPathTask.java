package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.movement.MovementService;
import com.krakenplugins.example.MiningPlugin;
import com.krakenplugins.example.script.AbstractTask;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class FollowPathTask extends AbstractTask {

    @Inject
    private MiningPlugin plugin;

    @Inject
    private MovementService movementService;

    private WorldPoint lastPosition;
    private int stuckCounter = 0;

    @Override
    public boolean validate() {
        return !plugin.getCurrentPath().isEmpty();
    }

    @Override
    public int execute() {
        WorldPoint nextPoint = plugin.getCurrentPath().get(0);

        // TODO Needs to take larger "leaps" this waits 8 ticks before it even clicks a point,
        // instead click a super far away point on the path so char is moving
        if (ctx.players().local().raw().getWorldLocation().distanceTo(nextPoint) < 8) {
            plugin.getCurrentPath().remove(0);
            stuckCounter = 0;
            lastPosition = null;
            return 0;
        }

        if (lastPosition != null && lastPosition.distanceTo(ctx.players().local().raw().getWorldLocation()) == 0) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
        }

        if (stuckCounter > 5) {
            plugin.getCurrentPath().clear();
            stuckCounter = 0;
            lastPosition = null;
            return 0;
        }

        movementService.moveTo(nextPoint);
        lastPosition = ctx.players().local().raw().getWorldLocation();

        return 300;
    }

    @Override
    public String status() {
        return "Following path";
    }
}
