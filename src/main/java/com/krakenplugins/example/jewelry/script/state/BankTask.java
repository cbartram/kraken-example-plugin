package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;
import static com.krakenplugins.example.jewelry.script.JewelryScript.SAPPHIRE;

@Slf4j
public class BankTask extends AbstractTask {

    private static final WorldPoint EDGEVILLE_BANK = new WorldPoint(3094, 3495, 0);

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
        ctx.getMouse().move(necklace.raw());
        necklace.depositAll();
        SleepService.sleepUntil(() -> ctx.inventory().withName("Sapphire Necklace").stream().findAny().isEmpty(), 3000);

        // Withdraw 13 Sapphire and 13 gold bar
        ctx.bankInventory().withId(GOLD_BAR);
        ctx.bankInventory().withId(SAPPHIRE);


        bankService.close();
        return RandomService.between(600, 1000);
    }

    @Override
    public String status() {
        return "Banking";
    }
}
