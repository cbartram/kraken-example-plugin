package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.script.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.MiningPlugin.BANK_LOCATION;

@Slf4j
@Singleton
public class BankingTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private SleepService sleepService;

    @Inject
    private WalkToBankTask walkToBankTask;

    @Override
    public boolean validate() {
        boolean playerInventoryFull = ctx.inventory().isFull();
        boolean playerInBank = ctx.players().local().isInArea(BANK_LOCATION);
        return playerInventoryFull && bankService.isOpen() && playerInBank;
    }

    @Override
    public int execute() {
        BankInventoryEntity iron = ctx.bankInventory().withName("Iron ore").first();
        ctx.getMouse().move(iron.raw());
        iron.depositAll();
        sleepService.sleepUntil(() -> ctx.inventory().withName("Iron ore").stream().findAny().isEmpty(), 3000);

        // TODO Get the close widget and move mouse to it

        bankService.close();
        walkToBankTask.setArrivedAtIntermediatePoint(false);
        return 1000;
    }

    @Override
    public String status() {
        return "Depositing Ore";
    }
}
