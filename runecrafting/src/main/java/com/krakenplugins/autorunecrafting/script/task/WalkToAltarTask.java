package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.hasEssence;

@Slf4j
@Singleton
public class WalkToAltarTask extends AbstractTask {

    private static final WorldPoint RUINS_TILE = new WorldPoint(2984, 3291, 0);

    /** Close enough for EnterAltarTask to take over. */
    private static final int ARRIVAL_TILES = 5;

    @Inject
    private AutoRunecraftingPlugin plugin;

    @Inject
    private BankService bankService;

    @Inject
    private Walker walker;

    @Override
    public boolean validate() {
        // Runs after EnterAltarTask, so anything still holding essence has not reached the ruins yet.
        // No area check: the script walks out of the bank area, which would make one un-validate
        // itself part way through the trip.
        return hasEssence(ctx);
    }

    @Override
    public int execute() {
        plugin.setTargetBankBooth(null);
        bankService.close();

        // walkTo plans, walks whatever of the route is loaded, handles the doors and gates on the way
        // and re-plans from wherever the player ends up. It returns immediately when already inside
        // the tolerance, so there is no arrival check to make here.
        WalkResult result = walker.walkTo(RUINS_TILE, ARRIVAL_TILES);
        if (!result.isSuccess()) {
            log.warn("Walk to the mysterious ruins failed: {}", result);
            return RandomService.between(1500, 3000);
        }

        return RandomService.between(600, 1200);
    }

    @Override
    public String status() {
        return "Walking to Altar";
    }
}
