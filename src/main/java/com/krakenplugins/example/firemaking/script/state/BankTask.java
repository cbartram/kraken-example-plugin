package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.example.firemaking.FiremakingPlugin.BANK_BOOTH_GAME_OBJECT;
import static com.krakenplugins.example.firemaking.FiremakingPlugin.BANK_LOCATION;

@Slf4j
public class BankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private FiremakingConfig config;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() &&
                !bankService.isOpen() &&
                ctx.gameObjects().withId(BANK_BOOTH_GAME_OBJECT).stream().findAny().isPresent() &&
                !ctx.players().local().isInArea(BANK_LOCATION, 3) &&
                ctx.inventory().withName(config.logName()).count() == 0;
    }

    @Override
    public int execute() {
        GameObjectEntity bankBooth = ctx.gameObjects().withId(BANK_BOOTH_GAME_OBJECT).nearest();

        if(bankBooth != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(bankBooth.raw());
            }
            bankBooth.interact("Bank");
            log.info("Opening Bank and sleeping");
            SleepService.sleepUntil(() -> bankService.isOpen(), 10000);
        }

        return 1200;
    }

    @Override
    public String status() {
        return "Moving to Bank";
    }
}
