package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.ui.processing.ProcessingService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

@Slf4j
public class CookFish extends PriorityTask {
    private static final int BARBARIAN_VILLAGE_FIRE = 43475;
    private static final int COOKING_ANIM = 897;

    @Inject
    private FishingConfig config;

    @Inject
    private ProcessingService processingService;

    @Override
    public boolean validate() {
        // Note: isIdle() can be tricky because it returns true during the 1-tick gap between cooking fish.
        // The logic in execute() now handles this by waiting for the animation to start.
        return ctx.inventory().isFull() && (ctx.inventory().hasItem(335) || ctx.inventory().hasItem(331)) &&
                ctx.players().local().isInArea(FishingLocation.BARBARIAN_VILLAGE.getLocation(), 15) &&
                ctx.players().local().isIdle() &&
                config.barbVillageCook();
    }

    private boolean isProcessingInterfaceOpen() {
        // 17694720
        Widget widget = ctx.getClient().getWidget(InterfaceID.Skillmulti.UNIVERSE);
        if(widget == null) {
            return false;
        }

        return !widget.isSelfHidden();
    }

    @Override
    public int execute() {
        // If we are currently animating (cooking), we return a short sleep to let the action continue.
        // This prevents spamming logic while the player is busy.
        if (ctx.players().local().raw().getAnimation() == COOKING_ANIM) {
            log.info("Player cooking already, waiting");
            return 600;
        }

        // logic: If the menu is open, we do NOT want to click the fire again.
        if (isProcessingInterfaceOpen()) {
            if (processingService.getAmount() != 28) {
                processingService.setAmount(28);
            }

            ctx.runOnClientThread(() -> processingService.process(333));

            // After we click confirm, there is a delay before the player starts animating (897).
            // If we don't sleep here, the script loops, sees the player is "Idle" (not yet animating),
            // and tries to click the fire again.
            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() == COOKING_ANIM, 6000);
            return 600;
        }

        // 3. Interact with Fire
        // We only reach here if we aren't cooking AND the interface isn't open.
        GameObjectEntity fire = ctx.gameObjects().withId(BARBARIAN_VILLAGE_FIRE).nearest();
        if (fire != null && fire.interact("Cook")) {
            // Wait for the interface to open
            SleepService.sleepUntilTrue(() -> processingService.isOpen(), 400, 5000);
        }

        return 0;
    }

    @Override
    public String status() {
        return "Cooking Fish";
    }

    @Override
    public int getPriority() {
        return 100;
    }
}