package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.jewelry.script.JewelryScript.*;

@Slf4j
@Singleton
public class OpenBankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Override
    public boolean validate() {
        return !ctx.inventory().hasItem(GOLD_BAR) && !ctx.inventory().hasItem(SAPPHIRE)
                && ctx.players().local().isIdle() && !bankService.isOpen();
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withId(BANK_BOOTH_ID).nearest();
        if(bankBooth != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(bankBooth.raw());
            }

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
