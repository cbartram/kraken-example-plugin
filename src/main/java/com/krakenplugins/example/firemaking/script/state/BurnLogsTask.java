package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;

import java.util.Random;

public class BurnLogsTask extends AbstractTask {

    @Inject
    private FiremakingPlugin plugin;

    @Inject
    private FiremakingConfig config;

    private final Random random = new Random();

    @Override
    public boolean validate() {
        return !ctx.inventory().isEmpty() && ctx.players().local().isIdle();
    }

    @Override
    public int execute() {
        InventoryEntity tinderbox = ctx.inventory().withName("Tinderbox").first();
        InventoryEntity logs = ctx.inventory().withName(config.logName()).first();

        if (tinderbox != null && logs != null) {
            if (config.useMouse()) {
                ctx.getMouse().move(tinderbox.raw());
            }

            if (random.nextBoolean()) {
                // use logs on tinderbox
                if (config.useMouse()) {
                    ctx.getMouse().move(logs.raw());
                }
                logs.useOn(tinderbox.raw());
            } else {
                // Use tinderbox on logs
                if (config.useMouse()) {
                    ctx.getMouse().move(logs.raw());
                }
                tinderbox.useOn(logs.raw());
            }
            SleepService.sleepUntil(() -> ctx.players().local().isIdle(), RandomService.between(2000, 4000));
        }

        return 600;
    }

    @Override
    public String status() {
        return "Burning Logs";
    }
}
