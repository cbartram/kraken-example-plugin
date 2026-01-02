package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPCComposition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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


            log.info("Opening Bank and sleeping");
            NPCComposition comp = ctx.runOnClientThread(banker.raw()::getComposition);
            if (comp == null || comp.getActions() == null) {
                return 0;
            }

            List<String> actions = Arrays.stream(comp.getActions()).collect(Collectors.toList());
            log.info("Banker actions: {}", actions);

            banker.interact("Bank");
            SleepService.sleepUntil(() -> bankService.isOpen() || bankService.isPinOpen(), 10000);
        }

        return 1200;
    }

    @Override
    public String status() {
        return "Moving to Bank";
    }
}
