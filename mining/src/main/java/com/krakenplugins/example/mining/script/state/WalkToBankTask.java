package com.krakenplugins.example.mining.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.mining.MiningPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.mining.MiningPlugin.BANK_LOCATION;
import static com.krakenplugins.example.mining.MiningPlugin.WALK_CONFIG;

@Slf4j
@Singleton
public class WalkToBankTask extends AbstractTask {

    @Inject
    private Walker walker;

    @Inject
    private MiningPlugin plugin;

    @Override
    public boolean validate() {
        return ctx.inventory().isFull()
                && !ctx.players().local().isInArea(plugin.getVarrockBank())
                && ctx.players().local().isIdle();
    }

    @Override
    public int execute() {
        plugin.setTargetRock(null);

        // The walker blocks until it arrives, re-planning and opening doors along the way, so by the
        // time this returns the script is either at the bank or done trying.
        WalkResult result = walker.walkTo(BANK_LOCATION, WALK_CONFIG);
        return result.isSuccess() ? 600 : plugin.reportWalkFailure(result, "Varrock east bank");
    }

    @Override
    public String status() {
        return "Pathing to bank";
    }
}
