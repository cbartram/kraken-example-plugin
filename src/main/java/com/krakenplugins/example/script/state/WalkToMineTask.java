package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.movement.Pathfinder;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.MiningPlugin;
import com.krakenplugins.example.script.AbstractTask;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.MiningPlugin.BANK_LOCATION;
import static com.krakenplugins.example.MiningPlugin.MINE_LOCATION;

@Singleton
public class WalkToMineTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private Pathfinder pathfinder;

    @Inject
    private MiningPlugin plugin;

    @Inject
    private SleepService sleepService;

    @Override
    public boolean validate() {
        // No iron ore in inventory, in bank area, bank interface is closed, and player is idle
        boolean playerInBank = ctx.players().local().isInArea(BANK_LOCATION, 3);
        return ctx.inventory().withName("iron ore").stream().findAny().isEmpty() && playerInBank && ctx.players().local().isIdle() && !bankService.isOpen();
    }

    @Override
    public int execute() {
        List<WorldPoint> path = pathfinder.findPath(ctx.players().local().raw().getWorldLocation(), MINE_LOCATION);
        plugin.getCurrentPath().clear();
        plugin.getCurrentPath().addAll(path);
        sleepService.sleepUntil(() -> ctx.players().local().isInArea(MINE_LOCATION, 2), 10000);
        return 1000;
    }

    @Override
    public String status() {
        return "Walking to Mine";
    }
}
