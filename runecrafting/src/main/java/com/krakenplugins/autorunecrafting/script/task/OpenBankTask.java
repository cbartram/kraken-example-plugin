package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.hasEssence;

@Slf4j
@Singleton
public class OpenBankTask extends AbstractTask {

    private static final String BANK_ACTION = "Bank";

    /** Long enough to cover the walk to the booth the click starts. */
    private static final long OPEN_TIMEOUT_MS = 8_000L;

    @Inject
    private BankService bankService;

    @Inject
    private AutoRunecraftingConfig config;

    @Inject
    private AutoRunecraftingPlugin plugin;

    @Override
    public boolean validate() {
        // Matching the action rather than a loc id reads the impostor-resolved composition, so it
        // finds the booth whichever variant of the deadman multiloc the scene holds. reachable() is
        // what hands the trip back to WalkToBankTask when the booths are in view but not in reach.
        // The scene scan goes last, it is the expensive check.
        return !bankService.isOpen()
                && ctx.players().local().isIdle()
                && !hasEssence(ctx)
                && ctx.gameObjects().withAction(BANK_ACTION).reachable().isPresent();
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withAction(BANK_ACTION).reachable().nearest().orElse(null);

        if (bankBooth == null) {
            log.info("No reachable bank booth found");
            return 600;
        }

        if (config.useMouse()) {
            ctx.getMouse().move(bankBooth.raw());
        }

        if (!bankBooth.interact(BANK_ACTION)) {
            log.warn("Bank action was not available on the booth");
            return 600;
        }

        plugin.setTargetBankBooth(bankBooth.raw());
        SleepService.sleepUntil(() -> bankService.isOpen() || bankService.isPinOpen(), OPEN_TIMEOUT_MS);

        // Nothing else in this script can dismiss the pin interface, and retrying the booth behind it
        // would just spin forever.
        if (bankService.isPinOpen()) {
            plugin.pauseScript("Bank pin interface is open, enter it and re-enable the plugin");
        }

        return 600;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
