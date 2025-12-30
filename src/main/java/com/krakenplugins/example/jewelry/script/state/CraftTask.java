package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.ui.processing.ProcessingService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

@Slf4j
public class CraftTask extends AbstractTask {

    private static final WorldPoint EDGEVILLE_FURNACE = new WorldPoint(3109, 3499, 0);
    private static final int FURNACE = 16469;
    private static final int SMELTING_ANIM = 1605; // TODO Check this

    @Inject
    private JewelryConfig config;

    @Inject
    private ProcessingService processingService;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() && ctx.players().local().isInArea(EDGEVILLE_FURNACE, 3) &&
                ctx.inventory().hasItem(2357) && ctx.inventory().hasItem(1607);
    }

    @Override
    public int execute() {
           if (ctx.players().local().raw().getAnimation() == SMELTING_ANIM) {
            log.info("Player SMELTING already, waiting");
            return 600;
        }

        // logic: If the menu is open, we do NOT want to click the fire again.
        if (processingService.isOpen()) {
            if (processingService.getAmount() != 28) {
                processingService.setAmount(28);
            }

            // TODO Figure this out
            ctx.runOnClientThread(() -> processingService.process(333));

            // After we click confirm, there is a delay before the player starts animating (897).
            // If we don't sleep here, the script loops, sees the player is "Idle" (not yet animating),
            // and tries to click the fire again.
            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() == SMELTING_ANIM, 6000);
            return 600;
        }

        GameObjectEntity fire = ctx.gameObjects().withId(FURNACE).nearest();
        if (fire != null && fire.interact("Smelt")) {
            // Wait for the interface to open
            SleepService.sleepUntilTrue(() -> processingService.isOpen(), 400, 5000);
        }

        return 0;
    }

    @Override
    public String status() {
        return "Crafting";
    }
}
