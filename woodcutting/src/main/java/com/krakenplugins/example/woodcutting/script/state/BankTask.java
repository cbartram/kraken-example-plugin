package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class BankTask extends AbstractTask {

    private static final String BANK_ACTION = "Bank";

    @Inject
    private BankService bankService;

    @Inject
    private WoodcuttingConfig config;

    @Inject
    private WoodcuttingPlugin plugin;

    @Override
    public boolean validate() {
        // No "away from the bank" check: clicking the booth is what walks us there, so a check like
        // that would stop validating the moment it mattered and strand a full inventory at the bank.
        return ctx.players().local().isIdle() && ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        // Found by action rather than object id, so any bank booth works and there is no id to keep
        // in step with the game.
        GameObjectEntity bankBooth = ctx.gameObjects().withAction(BANK_ACTION).nearest();

        if (bankBooth == null) {
            plugin.pauseScript("No bank in the scene to deposit logs at");
            return 600;
        }

        if (config.useMouse()) {
            ctx.getMouse().move(bankBooth.raw());
        }

        plugin.setTargetTree(null);

        if (!bankBooth.interact(BANK_ACTION)) {
            log.debug("Bank interaction did not register");
            return 600;
        }

        // The click walks us to the booth first, so this covers the whole trip.
        if (!SleepService.sleepUntil(() -> bankService.isOpen() || bankService.isPinOpen(), 10000)) {
            log.warn("Bank never opened after clicking {}", bankBooth.getName());
        }

        return 600;
    }

    @Override
    public String status() {
        return "Moving to Bank";
    }
}
