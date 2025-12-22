package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.MiningPlugin;
import com.krakenplugins.example.script.AbstractTask;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.MiningPlugin.MINE_LOCATION;

@Singleton
public class WalkToMineTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private LocalPathfinder pathfinder;

    @Inject
    private MiningPlugin plugin;

    @Override
    public boolean validate() {
        boolean playerNotInMine = !ctx.players().local().isInArea(MINE_LOCATION, 3);
        return !ctx.inventory().isFull()
                && playerNotInMine
                && ctx.players().local().isIdle()
                && !bankService.isOpen()
                && plugin.getCurrentPath().isEmpty();
    }

    @Override
    public int execute() {
        List<WorldPoint> path = pathfinder.findSparsePath(ctx.players().local().raw().getWorldLocation(), MINE_LOCATION);
        plugin.getCurrentPath().addAll(path);
        return 300;
    }

    @Override
    public String status() {
        return "Pathing to Mine";
    }
}
