package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import static com.krakenplugins.example.jewelry.JewelryPlugin.WALK_CONFIG;
import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Singleton
@Slf4j
public class WalkToGrandExchange extends AbstractTask {

    private static final WorldPoint GRAND_EXCHANGE = new WorldPoint(3164, 3486, 0);

    // How long the bank interface has to close before the walk starts anyway.
    private static final long CLOSE_TIMEOUT_MS = 3000;

    @Inject
    private JewelryPlugin plugin;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Inject
    private Walker walker;

    @Override
    public boolean validate() {
        // The bank has to be open to read what is in it, which is also the only moment the script
        // learns it has run out of materials.
        if (!config.enableResupply() || !bankService.isOpen()) {
            return false;
        }

        if (!ctx.players().local().isInArea(plugin.getEdgevilleBank())) {
            return false;
        }

        return ctx.bank().withId(GOLD_BAR).first() == null
                || ctx.bank().withId(config.jewelry().getSecondaryGemId()).first() == null;
    }

    @Override
    public int execute() {
        bankService.close();
        SleepService.sleepUntil(bankService::isClosed, CLOSE_TIMEOUT_MS);

        // The walker blocks until it arrives, re-planning and opening doors along the way, so by the
        // time this returns the script is either at the Grand Exchange or done trying.
        WalkResult result = walker.walkTo(GRAND_EXCHANGE, WALK_CONFIG);
        return result.isSuccess() ? 600 : plugin.reportWalkFailure(result, "the Grand Exchange");
    }

    @Override
    public String status() {
        return "Walking to G.E.";
    }
}
