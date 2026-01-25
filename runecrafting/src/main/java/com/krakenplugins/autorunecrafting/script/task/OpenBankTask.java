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
        boolean bankBoothPresent = ctx.gameObjects().withId(BANK_BOOTH_ID).nearest() != null;
        return (!ctx.inventory().hasItem(RUNE_ESSENCE) && !ctx.inventory().hasItem(PURE_ESSENCE))
                && ctx.players().local().isIdle() && !bankService.isOpen() && bankBoothPresent;
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withId(BANK_BOOTH_ID).sortByDistance().random();

        if(bankBooth != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(bankBooth.raw());
            }

            plugin.setTargetBankBooth(bankBooth);
            bankBooth.interact("Bank");
            SleepService.sleepUntil(() -> bankService.isOpen(), 8000);
        } else {
            log.info("Bank booth entity not found ");
        }
        return 0;
    }

    @Override
    public String status() {
        return "Opening Bank";
    }
}
