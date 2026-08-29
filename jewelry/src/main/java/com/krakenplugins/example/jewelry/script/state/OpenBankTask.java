package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.krakenplugins.example.jewelry.script.JewelryScript.BANK_BOOTH;
import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Slf4j
@Singleton
public class OpenBankTask extends AbstractTask {

    // How long the booth has to answer a click before the loop clicks it again.
    private static final long OPEN_TIMEOUT_MS = 10000;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Inject
    private JewelryPlugin plugin;

    @Override
    public boolean validate() {
        // Missing either material means the trip is over and it is time to restock. The Grand
        // Exchange has its own banker, so this task stays out of it.
        return !bankService.isOpen()
                && ctx.players().local().isIdle()
                && !ctx.players().local().isInArea(plugin.getGrandExchange())
                && !ctx.inventory().hasItems(GOLD_BAR, config.jewelry().getSecondaryGemId());
    }

    @Override
    public int execute() {
        // The two nearest booths, so the script does not click the same one on every trip.
        List<GameObjectEntity> booths = ctx.gameObjects().withId(BANK_BOOTH).sortByDistance().take(2);
        if (booths.isEmpty()) {
            plugin.halt("Out of materials with no bank booth in the scene to restock at");
            return 0;
        }

        GameObjectEntity booth = booths.get(RandomService.between(0, booths.size() - 1));
        plugin.moveMouseTo(booth.raw());

        if (!booth.interact("Bank")) {
            log.info("Bank action was not available on the booth at {}", booth.raw().getWorldLocation());
            return 600;
        }

        plugin.setTargetBankBooth(booth);
        SleepService.sleepUntil(bankService::isOpen, OPEN_TIMEOUT_MS);
        return 600;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
