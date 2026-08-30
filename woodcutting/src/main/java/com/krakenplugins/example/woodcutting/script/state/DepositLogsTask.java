package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.InterfaceID;

@Slf4j
@Singleton
public class DepositLogsTask extends AbstractTask {

    /** Matches every log type, so the task does not need to know which tree is being chopped. */
    private static final String LOG_NAME = "logs";

    @Inject
    private BankService bankService;

    @Inject
    private WoodcuttingConfig config;

    @Inject
    private WoodcuttingPlugin plugin;

    @Override
    public boolean validate() {
        // An open bank is the only condition. Adding an area or full-inventory check here would leave
        // states where no task at all validates and the script parks behind the interface forever.
        return bankService.isOpen();
    }

    @Override
    public int execute() {
        BankInventoryEntity logs = ctx.bankInventory().nameContains(LOG_NAME).random().orElse(null);

        if (logs == null) {
            if (ctx.inventory().isFull()) {
                plugin.pauseScript("Inventory is full but holds no logs to deposit");
            }
            bankService.close();
            return RandomService.between(600, 1400);
        }

        if (config.useMouse()) {
            ctx.getMouse().move(logs.raw());
        }

        logs.depositAll();

        // Deposit-All clears one item id, so a mixed load needs another pass. Leaving the bank open
        // and returning is what gives it one.
        if (!SleepService.sleepUntil(() -> ctx.inventory().nameContains(LOG_NAME).isEmpty(), 3000)) {
            log.debug("Logs still in the inventory after depositing");
            return 600;
        }

        if (config.useMouse()) {
            WidgetEntity closeButton = ctx.widgets().get(InterfaceID.Bankmain.FRAME);
            if (closeButton != null) {
                ctx.getMouse().move(closeButton.raw());
            }
        }

        bankService.close();
        return RandomService.between(600, 1400);
    }

    @Override
    public String status() {
        return "Depositing Logs";
    }
}
