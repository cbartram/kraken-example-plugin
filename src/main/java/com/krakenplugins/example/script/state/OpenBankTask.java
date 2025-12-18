package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.script.AbstractTask;

@Singleton
public class OpenBankTask extends AbstractTask {

    private static final int BANK_BOOTH_ID = 1234;

    @Inject
    private SleepService sleepService;

    @Inject
    private BankService bankService;

    @Override
    public boolean validate() {
        return ctx.gameObjects().within(7).withId(BANK_BOOTH_ID).stream().findAny().isPresent() && ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        ctx.gameObjects().withId(BANK_BOOTH_ID).nearest().interact("Bank");
        sleepService.sleepUntil(() -> bankService.isOpen(), 10000);
        return 500;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
