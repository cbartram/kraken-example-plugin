package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.*;

@Slf4j
@Singleton
public class OpenBankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private AutoRunecraftingConfig config;

    @Inject
    private AutoRunecraftingPlugin plugin;

    @Override
    public boolean validate() {
        return (!ctx.inventory().hasItem(RUNE_ESSENCE) && !ctx.inventory().hasItem(PURE_ESSENCE))
                && ctx.players().local().isIdle() && !bankService.isOpen() && !ctx.players().local().isInArea(plugin.getFaladorBank());
    }

    @Override
    public int execute() {
        List<GameObjectEntity> bankBooths = ctx.gameObjects().withId(BANK_BOOTH_ID).sortByDistance().take(2);
        GameObjectEntity bankBooth = bankBooths.get((new Random()).nextInt(bankBooths.size()));

        if(bankBooth != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(bankBooth.raw());
            }

            plugin.setTargetBankBooth(bankBooth);
            log.info("Opening bank");
            bankBooth.interact("Bank");
            SleepService.sleepUntil(() -> bankService.isOpen(), 8000);
        }
        return 0;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
