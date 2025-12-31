package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;

import static com.krakenplugins.example.jewelry.script.JewelryScript.*;

@Singleton
public class OpenFurnaceTask extends AbstractTask {

    @Inject
    private JewelryConfig config;

    @Inject
    private CraftTask craftTask;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryPlugin jewelryPlugin;

    @Override
    public boolean validate() {
        return ctx.players().local().isInArea(EDGEVILLE_BANK, 5) && ctx.inventory().hasItem(GOLD_BAR) && ctx.inventory().hasItem(config.jewelry().getSecondaryGemId())
                && ctx.players().local().isIdle() && !bankService.isOpen();
    }

    @Override
    public int execute() {
        jewelryPlugin.setTargetBankBooth(null);
        GameObjectEntity furnace = ctx.gameObjects().withId(FURNACE_GAME_OBJECT).nearest();
        if(furnace != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(furnace.raw());
            }

            furnace.interact("Smelt");
            SleepService.sleepUntil(() -> craftTask.isCraftingInterfaceOpen(), 15000);
        }
        return 0;
    }

    @Override
    public String status() {
        return "Walking to Furnace";
    }
}
