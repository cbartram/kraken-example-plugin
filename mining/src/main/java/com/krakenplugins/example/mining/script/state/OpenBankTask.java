package com.krakenplugins.example.mining.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.pathfinding.GlobalPathfinder;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.mining.MiningPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.mining.MiningPlugin.BANK_BOOTH;

@Slf4j
@Singleton
public class OpenBankTask extends AbstractTask {

    // How long the booth has to answer a click before the loop clicks it again.
    private static final long OPEN_TIMEOUT_MS = 10000;

    @Inject
    private BankService bankService;

    @Inject
    private MiningPlugin plugin;

    @Inject
    private GlobalPathfinder globalPathfinder;

    @Override
    public boolean validate() {
        return !bankService.isOpen()
                && ctx.inventory().isFull()
                && ctx.players().local().isInArea(plugin.getVarrockBank());
    }

    @Override
    public int execute() {
        globalPathfinder.clearLastResult();

        GameObjectEntity booth = ctx.gameObjects().withId(BANK_BOOTH).nearest().orElse(null);
        if (booth == null) {
            plugin.halt("Standing in the Varrock east bank with no bank booth in the scene");
            return 0;
        }

        plugin.moveMouseTo(booth.raw());
        if (!booth.interact("Bank")) {
            log.info("Bank action was not available on the booth at {}", booth.raw().getWorldLocation());
            return 600;
        }

        SleepService.sleepUntil(bankService::isOpen, OPEN_TIMEOUT_MS);
        return 600;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
