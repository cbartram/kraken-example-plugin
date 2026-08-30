package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class BankTask extends AbstractTask {

    private static final String BANK_ACTION = "Bank";

    @Inject
    private BankService bankService;

    @Inject
    private FiremakingPlugin plugin;

    @Inject
    private FiremakingConfig config;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() &&
                !bankService.isOpen() &&
                ctx.inventory().withName(config.logName()).count() == 0;
    }

    @Override
    public int execute() {
        // No reachable() filter here: bankers stand behind a counter, so their own tile is usually
        // unwalkable even though they can be interacted with.
        NpcEntity banker = ctx.npcs()
                .withName("Banker")
                .withAction(BANK_ACTION)
                .nearest()
                .orElse(null);

        if (banker == null) {
            log.debug("No banker with a Bank option nearby");
            return 1200;
        }

        plugin.setTargetBanker(banker.raw());
        plugin.setTargetFire(null);

        if (config.useMouse()) {
            ctx.getMouse().move(banker.raw());
        }

        banker.interact(BANK_ACTION);
        SleepService.sleepUntil(() -> bankService.isOpen() || bankService.isPinOpen(), 10000);

        return 1200;
    }

    @Override
    public String status() {
        return "Moving to Bank";
    }
}
