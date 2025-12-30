package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;

@Singleton
public class OpenBankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    private static final int BANK_BOOTH_ID = 10355;

    @Override
    public boolean validate() {
        return ctx.players().local().isInArea(CraftTask.EDGEVILLE_FURNACE, 3) && !ctx.inventory().hasItem(2357) && !ctx.inventory().hasItem(1607)
                && ctx.players().local().isIdle();
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withId(BANK_BOOTH_ID).nearest();
        if(bankBooth != null) {
            bankBooth.interact("Bank");
            SleepService.sleepUntil(() -> bankService.isOpen(), 8000);
        }
        return 0;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
