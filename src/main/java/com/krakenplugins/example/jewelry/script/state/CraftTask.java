package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

import static com.krakenplugins.example.jewelry.script.JewelryScript.*;

@Slf4j
public class CraftTask extends AbstractTask {

    private static final int SMELTING_ANIM = 899;
    private static final int SAPPHIRE_NECKLACE_WIDGET = 29229080;

    @Inject
    private JewelryConfig config;

    @Override
    public boolean validate() {
        // FIX 1: Removed 'isIdle()' check.
        // We want this task to be active even if we are currently animating,
        // so we can manage the 'wait' inside execute().
        return ctx.players().local().isInArea(EDGEVILLE_FURNACE, 3) &&
                ctx.inventory().hasItem(GOLD_BAR) && ctx.inventory().hasItem(SAPPHIRE);
    }

    public boolean isCraftingInterfaceOpen() {
        Widget w = ctx.getClient().getWidget(InterfaceID.CraftingGold.UNIVERSE);
        if(w == null) return false;
        return !w.isSelfHidden();
    }

    @Override
    public int execute() {
        // Check if we are already animating
        if (ctx.players().local().raw().getAnimation() == SMELTING_ANIM) {
            log.info("Player SMELTING, waiting...");
            return 1200; // Sleep while working
        }

        // FIX 2: Handle the "Animation Gap"
        // If we seem idle, wait 1 tick (600ms) and check again.
        // This prevents clicking the furnace during the split-second between items.
        if (ctx.players().local().raw().getAnimation() != -1) { // Broader check for any animation
            log.info("Animation gap, sleep for 1 tick");
            return 600;
        }

        // Handle interface if it's open
        if (isCraftingInterfaceOpen()) {
            SleepService.tick();
            WidgetEntity widget = ctx.widgets().get(SAPPHIRE_NECKLACE_WIDGET);
            if(widget != null && !widget.isNull()) {
                if(config.useMouse()) {
                    ctx.getMouse().move(widget.raw());
                }
                widget.interact("make sapphire necklace");
            }
            // Wait for animation to actually start before looping
            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() == SMELTING_ANIM, 6000);
            return 600;
        }

        // FIX 3: The "Debounce" Logic
        // We are not animating, and interface is closed.
        // Before clicking furnace, ensure we didn't just stop for 1 tick.
        SleepService.sleep(600);
        if (ctx.players().local().raw().getAnimation() == SMELTING_ANIM) {
            return 600; // We were just between items, go back to sleep
        }

        log.info("Crafting interface closed and not animating. Clicking furnace.");
        GameObjectEntity furnace = ctx.gameObjects().withId(FURNACE_GAME_OBJECT).nearest();
        if (furnace != null && furnace.interact("Smelt")) {
            SleepService.sleepUntilTrue(this::isCraftingInterfaceOpen, 400, 5000);
        }

        return 0;
    }

    @Override
    public String status() {
        return "Crafting Necklaces";
    }
}