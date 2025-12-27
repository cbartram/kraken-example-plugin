package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.BANK_BOOTH_GAME_OBJECT;

public class BankTask extends AbstractTask {

    @Inject
    private SleepService sleepService;

    @Inject
    private BankService bankService;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() &&
                ctx.gameObjects().withId(BANK_BOOTH_GAME_OBJECT).stream().findAny().isPresent() &&
                ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withId(BANK_BOOTH_GAME_OBJECT).nearest();

        if(bankBooth != null) {
            ctx.getMouse().move(bankBooth.raw());
            bankBooth.interact("Bank");
            sleepService.sleepUntil(() -> bankService.isOpen(), 10000);
        }

        return 1200;
    }

    @Override
    public String status() {
        return "Moving to Bank";
    }
}
