package com.krakenplugins.example.firemaking.script.state;

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
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;

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
        // An open bank is proof enough that we are at one, so there is no area check here. Requiring one
        // would deadlock the script whenever the bank is opened from a tile outside the configured area.
        return !ctx.inventory().hasItem(config.logName()) && bankService.isOpen();
    }

    @Override
    public int execute() {
        BankEntity logs = ctx.bank().withName(config.logName()).first().orElse(null);
        if (logs == null) {
            plugin.pauseScript("Out of " + config.logName());
            bankService.close();
            return 600;
        }

        if (!ctx.inventory().hasItem(ItemID.TINDERBOX)) {
            log.info("Withdrawing Tinderbox");
            BankEntity tinderbox = ctx.bank().withId(ItemID.TINDERBOX).first().orElse(null);
            if (tinderbox == null) {
                plugin.pauseScript("No tinderbox in the bank");
                bankService.close();
                return 600;
            }

            tinderbox.withdrawOne();
            SleepService.sleepUntil(() -> ctx.inventory().hasItem(ItemID.TINDERBOX), 3000);
        }

        plugin.setTargetBanker(null);

        if (config.useMouse()) {
            ctx.getMouse().move(logs.raw());
        }

        logs.withdrawAll();

        // Waiting on the logs rather than a full inventory, since the bank may hold less than a load.
        SleepService.sleepUntil(() -> ctx.inventory().hasItem(config.logName()), 3000);

        if (config.useMouse()) {
            WidgetEntity closeButton = ctx.widgets().withId(InterfaceID.Bankmain.FRAME).first().orElse(null);
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
