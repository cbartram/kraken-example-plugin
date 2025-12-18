package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.script.AbstractTask;

import static com.krakenplugins.example.MiningPlugin.BANK_LOCATION;

@Singleton
public class BankingTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private SleepService sleepService;

    @Override
    public boolean validate() {
        boolean playerInventoryFull = ctx.inventory().isFull();
        boolean playerInBank = ctx.players().local().isInArea(BANK_LOCATION);
        return playerInventoryFull && bankService.isOpen() && playerInBank;
    }

    @Override
    public int execute() {
          ctx.bankInventory().withName("Iron ore").first().depositAll();
          sleepService.sleepUntil(() -> ctx.inventory().withName("Iron ore").stream().findAny().isEmpty(), 3000);
          return 1000;
    }

    @Override
    public String status() {
        return "Depositing Ore";
    }
}
