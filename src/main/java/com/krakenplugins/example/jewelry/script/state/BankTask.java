package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.jewelry.script.JewelryScript.*;

@Slf4j
public class BankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Override
    public boolean validate() {
          return ctx.players().local().isIdle() && ctx.players().local().isInArea(EDGEVILLE_BANK, 3) &&
                !ctx.inventory().hasItem(GOLD_BAR) && !ctx.inventory().hasItem(SAPPHIRE) && bankService.isOpen();
    }

    @Override
    public int execute() {
        BankInventoryEntity necklace = ctx.bankInventory().withName("Sapphire Necklace").first();

        if(config.useMouse()) {
            ctx.getMouse().move(necklace.raw());
        }

        if(necklace != null) {
            necklace.depositAll();
            SleepService.sleepUntil(() -> ctx.inventory().withName("Sapphire Necklace").stream().findAny().isEmpty(), 3000);
        }

        // Withdraw 13 Sapphire and 13 gold bar
        BankEntity goldBar = ctx.bank().withId(GOLD_BAR).first();
        if(goldBar != null) {
            log.info("Withdrawing Gold");
            goldBar.withdraw(13);
            SleepService.sleep(600, 1800);
        }

        BankEntity sapphire = ctx.bank().withId(SAPPHIRE).first();
        if(sapphire != null) {
            log.info("Withdrawing Sapphire");
            sapphire.withdraw(13);
            SleepService.sleep(600, 1800);
        }

        bankService.close();
        return RandomService.between(600, 1000);
    }

    @Override
    public String status() {
        return "Banking";
    }
}
