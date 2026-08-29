package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import static com.krakenplugins.example.jewelry.JewelryPlugin.WALK_CONFIG;

@Slf4j
@Singleton
public class WalkToEdgeville extends AbstractTask {

    private static final WorldPoint EDGEVILLE_BANK = new WorldPoint(3096, 3496, 0);

    @Inject
    private Walker walker;

    @Inject
    private JewelryPlugin plugin;

    @Inject
    private JewelryConfig config;

    @Inject
    private PurchaseSuppliesTask purchaseSuppliesTask;

    @Override
    public boolean validate() {
        // Head home once the trip is done, or once resupply is switched off underneath it, which
        // leaves nothing at the Grand Exchange for any task to claim.
        return ctx.players().local().isInArea(plugin.getGrandExchange())
                && (purchaseSuppliesTask.isPurchaseComplete() || !config.enableResupply());
    }

    @Override
    public int execute() {
        // The walker blocks until it arrives, re-planning and opening doors along the way, so by the
        // time this returns the script is either at the bank or done trying.
        WalkResult result = walker.walkTo(EDGEVILLE_BANK, WALK_CONFIG);
        if (!result.isSuccess()) {
            return plugin.reportWalkFailure(result, "the Edgeville bank");
        }

        // The trip is over, so an empty bank is free to start another one.
        purchaseSuppliesTask.setPurchaseComplete(false);
        return 600;
    }

    @Override
    public String status() {
        return "Walking to Edgeville";
    }
}
