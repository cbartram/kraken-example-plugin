package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.walker.WalkResult;
import com.kraken.api.service.walker.Walker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.hasEssence;

@Slf4j
@Singleton
public class WalkToBankTask extends AbstractTask {

    private static final WorldPoint FALADOR_BANK = new WorldPoint(3012, 3356, 0);

    /** Close enough for OpenBankTask to reach a booth. */
    private static final int ARRIVAL_TILES = 4;

    @Inject
    private Walker walker;

    @Override
    public boolean validate() {
        // Last in the task list, so everything that could bank or craft where it stands has already
        // been offered the chance. No area check: the script walks out of the altar area, which would
        // make one un-validate itself part way through the trip.
        return !hasEssence(ctx);
    }

    @Override
    public int execute() {
        // The bank is well outside the loaded scene from the altar, which is exactly the case walkTo
        // handles: it walks to the scene edge, lets the scene shift, and re-plans from there.
        WalkResult result = walker.walkTo(FALADOR_BANK, ARRIVAL_TILES);
        if (!result.isSuccess()) {
            log.warn("Walk to Falador east bank failed: {}", result);
            return RandomService.between(1500, 3000);
        }

        return RandomService.between(600, 1200);
    }

    @Override
    public String status() {
        return "Walking to Bank";
    }
}
