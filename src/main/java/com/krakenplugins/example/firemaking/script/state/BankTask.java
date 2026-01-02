package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankTask extends AbstractTask {

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
                ctx.npcs().withName("Banker").stream().findAny().isPresent() &&
                !ctx.players().local().isInArea(plugin.getBankLocation()) &&
                ctx.inventory().withName(config.logName()).count() == 0;
    }

    @Override
    public int execute() {
        NpcEntity banker = ctx.npcs().withName("Banker").nearest();
        if(banker != null) {
            plugin.setTargetBanker(banker.raw());
            if(config.useMouse()) {
                ctx.getMouse().move(banker.raw());
            }

            banker.interact("Bank");
            log.info("Opening Bank and sleeping");
            SleepService.sleepUntil(() -> bankService.isOpen() || bankService.isPinOpen(), 10000);
        }

        return 1200;
    }

    @Override
    public String status() {
        return "Moving to Bank";
    }
}
