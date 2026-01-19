package com.krakenpluging.example.firemaking.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class WithdrawLogsTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private FiremakingConfig config;

    @Inject
    private FiremakingPlugin plugin;

    @Override
    public boolean validate() {
        boolean playerInventoryEmpty = !ctx.inventory().hasItem(config.logName());
        boolean playerInBank = ctx.players().local().isInArea(plugin.getBankLocation());
        return playerInventoryEmpty && bankService.isOpen() && playerInBank;
    }

    @Override
    public int execute() {
        BankEntity logs = ctx.bank().withName(config.logName()).first();

        if(!ctx.inventory().hasItem(590)) {
            log.info("Withdrawing Tinderbox");
            BankEntity tinderbox = ctx.bank().withName("Tinderbox").first();
            if(tinderbox != null) {
                tinderbox.withdrawOne();
            }
        }

        if(plugin.getTargetBanker() != null) {
            plugin.setTargetBanker(null);
        }

        if(logs != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(logs.raw());
            }

            logs.withdrawAll();
            SleepService.sleepUntil(() -> ctx.inventory().isFull(), 3000);
        } else {
            log.info("No logs found in bank");
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
