package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.firemaking.FiremakingPlugin.BANK_LOCATION;

@Slf4j
public class WithdrawLogsTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private FiremakingConfig config;

    @Override
    public boolean validate() {
        boolean playerInventoryEmpty = ctx.inventory().withName(config.logName()).count() == 0;
        boolean playerInBank = ctx.players().local().isInArea(BANK_LOCATION, 6);
        return playerInventoryEmpty && bankService.isOpen() && playerInBank;
    }

    @Override
    public int execute() {
        BankEntity logs = ctx.bank().withName(config.logName()).first();

        if(logs != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(logs.raw());
            }

            logs.withdrawAll();
            SleepService.sleepUntil(() -> ctx.inventory().isFull(), 3000);
        } else {
            log.info("No logs found in bank");
            // Stop script or handle error
        }

        if(config.useMouse()) {
            WidgetEntity closeButton = ctx.widgets().withId(786434).first();
            if (closeButton != null) {
                ctx.getMouse().move(closeButton.raw());
            }
        }

        bankService.close();
        return RandomService.between(600, 1400);
    }

    @Override
    public String status() {
        return "Withdrawing Logs";
    }
}
