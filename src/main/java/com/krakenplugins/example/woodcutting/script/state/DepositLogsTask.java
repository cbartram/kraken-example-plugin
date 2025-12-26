package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.BANK_LOCATION;

public class DepositLogsTask extends AbstractTask {

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
        BankInventoryEntity logs = ctx.bankInventory().nameContains("logs").first();
        ctx.getMouse().move(logs.raw());
        logs.depositAll();
        sleepService.sleepUntil(() -> ctx.inventory().withName("logs").stream().findAny().isEmpty(), 3000);

        // TODO Get the close widget and move mouse to it
        WidgetEntity closeButton = ctx.widgets().withId(786434).first();
        if(closeButton != null) {
            ctx.getMouse().move(closeButton.raw());
        }

        bankService.close();
        return 1000;
    }

    @Override
    public String status() {
        return "Depositing Logs";
    }
}
