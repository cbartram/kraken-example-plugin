package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.movement.Pathfinder;
import com.krakenplugins.example.MiningPlugin;
import com.krakenplugins.example.script.AbstractTask;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.MiningPlugin.BANK_LOCATION;

@Singleton
public class WalkToBankTask extends AbstractTask {

    @Inject
    private Pathfinder pathfinder;

    @Inject
    private MiningPlugin plugin;

    @Override
    public boolean validate() {
        boolean playerNotInBank = !ctx.players().local().isInArea(BANK_LOCATION, 3);
        return ctx.inventory().isFull()
                && playerNotInBank
                && ctx.players().local().isIdle()
                && plugin.getCurrentPath().isEmpty();
    }

    @Override
    public int execute() {
        List<WorldPoint> path = pathfinder.findPath(ctx.players().local().raw().getWorldLocation(), BANK_LOCATION);
        plugin.getCurrentPath().addAll(path);
        return 300;
    }

    @Override
    public String status() {
        return "Pathing to bank";
    }
}
