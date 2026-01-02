package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.BANK_BOOTH_GAME_OBJECT;
import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.BANK_LOCATION;

@Slf4j
public class BankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private WoodcuttingConfig config;

    @Inject
    private WoodcuttingPlugin plugin;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() &&
                !bankService.isOpen() &&
                ctx.gameObjects().withId(BANK_BOOTH_GAME_OBJECT).stream().findAny().isPresent() &&
                !ctx.players().local().isInArea(BANK_LOCATION, 3) &&
                ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withId(BANK_BOOTH_GAME_OBJECT).nearest();

        if(bankBooth != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(bankBooth.raw());
            }

            plugin.setTargetTree(null);
            bankBooth.interact("Bank");
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
