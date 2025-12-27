package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.BANK_LOCATION;

public class DepositLogsTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private SleepService sleepService;

    @Inject
    private WoodcuttingConfig config;

    // TODO Bug here after deposit is complete it should move to the ChopLogsTask state but doesnt
    @Override
    public boolean validate() {
        boolean playerInventoryFull = ctx.inventory().isFull();
        boolean playerInBank = ctx.players().local().isInArea(BANK_LOCATION, 3);
        return playerInventoryFull && bankService.isOpen() && playerInBank;
    }

    @Override
    public int execute() {
        BankInventoryEntity logs = ctx.bankInventory().nameContains("logs").random();

        if(config.useMouse()) {
            ctx.getMouse().move(logs.raw());
        }

        logs.depositAll();
        sleepService.sleepUntil(() -> ctx.inventory().withName("logs").stream().findAny().isEmpty(), 3000);

        if(config.useMouse()) {
            WidgetEntity closeButton = ctx.widgets().withId(786434).first();
            if (closeButton != null) {
                ctx.getMouse().move(closeButton.raw());
            }
        }

        bankService.close();
        return 1000;
    }

    @Override
    public String status() {
        return "Depositing Logs";
    }
}
