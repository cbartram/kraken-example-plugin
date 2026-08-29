package com.krakenplugins.example.mining.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.mining.MiningPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.mining.MiningPlugin.MINE_LOCATION;
import static com.krakenplugins.example.mining.MiningPlugin.WALK_CONFIG;

@Slf4j
@Singleton
public class WalkToMineTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private Walker walker;

    @Inject
    private MiningPlugin plugin;

    @Override
    public boolean validate() {
        return !ctx.inventory().isFull()
                && !ctx.players().local().isInArea(plugin.getMiningArea())
                && ctx.players().local().isIdle();
    }

    @Override
    public int execute() {
        // Walking away from the booth is what closes the bank interface behind us, and this is the
        // only task that runs while it is open with room left in the inventory.
        bankService.close();

        // The walker blocks until it arrives, re-planning and opening doors along the way, so by the
        // time this returns the script is either in the mine or done trying.
        WalkResult result = walker.walkTo(MINE_LOCATION, WALK_CONFIG);
        return result.isSuccess() ? 600 : plugin.reportWalkFailure(result, "the south east Varrock mine");
    }

    @Override
    public String status() {
        return "Pathing to Mine";
    }
}
