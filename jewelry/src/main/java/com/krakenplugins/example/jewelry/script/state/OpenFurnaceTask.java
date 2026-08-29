package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.jewelry.script.JewelryScript.FURNACE;
import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Slf4j
@Singleton
public class OpenFurnaceTask extends AbstractTask {

    // The click both walks the player over from the bank and opens the interface, so the wait covers
    // the whole trip across the square.
    private static final long OPEN_TIMEOUT_MS = 15000;

    @Inject
    private JewelryConfig config;

    @Inject
    private CraftTask craftTask;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryPlugin plugin;

    @Override
    public boolean validate() {
        return !bankService.isOpen()
                && ctx.players().local().isIdle()
                && ctx.players().local().isInArea(plugin.getEdgevilleBank())
                && ctx.inventory().hasItems(GOLD_BAR, config.jewelry().getSecondaryGemId());
    }

    @Override
    public int execute() {
        plugin.setTargetBankBooth(null);

        GameObjectEntity furnace = ctx.gameObjects().withId(FURNACE).nearest();
        if (furnace == null) {
            plugin.halt("Standing in the Edgeville bank with no furnace in the scene");
            return 0;
        }

        plugin.moveMouseTo(furnace.raw());
        if (!furnace.interact("Smelt")) {
            log.info("Smelt action was not available on the furnace at {}", furnace.raw().getWorldLocation());
            return 600;
        }

        SleepService.sleepUntil(craftTask::isCraftingInterfaceOpen, OPEN_TIMEOUT_MS);
        return 600;
    }

    @Override
    public String status() {
        return "Walking to Furnace";
    }
}
