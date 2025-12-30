package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

import static com.krakenplugins.example.jewelry.script.JewelryScript.BANK_BOOTH_ID;
import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Slf4j
@Singleton
public class OpenBankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Inject
    private JewelryPlugin jewelryPlugin;

    @Override
    public boolean validate() {
        return !ctx.inventory().hasItem(GOLD_BAR) && !ctx.inventory().hasItem(config.jewelry().getSecondaryGemId())
                && ctx.players().local().isIdle() && !bankService.isOpen();
    }

    @Override
    public int execute() {

        // TODO Find another nearby bank booth not always this one

        List<GameObjectEntity> bankBooths = ctx.gameObjects().withId(BANK_BOOTH_ID).sortByDistance().take(2);
        GameObjectEntity bankBooth = bankBooths.get((new Random()).nextInt(bankBooths.size()));

        if(bankBooth != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(bankBooth.raw());
            }

            jewelryPlugin.setTargetBankBooth(bankBooth);
            log.info("Opening bank");
            bankBooth.interact("Bank");
            SleepService.sleepUntil(() -> bankService.isOpen(), 8000);
        }
        return 0;
    }

    @Override
    public String status() {
        return "Banking";
    }
}
