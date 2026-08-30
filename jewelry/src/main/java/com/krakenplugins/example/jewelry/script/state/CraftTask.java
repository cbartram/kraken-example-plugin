package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.gameval.InterfaceID;

import static com.krakenplugins.example.jewelry.script.JewelryScript.FURNACE;
import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Slf4j
@Singleton
public class CraftTask extends AbstractTask {

    // human_furnace: the animation for every furnace craft, not one per piece of jewellery.
    private static final int SMELTING_ANIM = 899;

    // How long to wait between checks while the batch is running.
    private static final int CRAFTING_POLL_MS = 1200;

    // How long the game has to start the animation, or draw the interface, after a click.
    private static final long RESPONSE_TIMEOUT_MS = 6000;

    @Inject
    private JewelryConfig config;

    @Inject
    private JewelryPlugin plugin;

    @Override
    public boolean validate() {
        // No idle check: this task owns the furnace for the whole batch and manages the waiting
        // itself, so it stays valid while the animation is running.
        return ctx.players().local().isInArea(plugin.getEdgevilleFurnace())
                && ctx.inventory().hasItems(GOLD_BAR, config.jewelry().getSecondaryGemId());
    }

    /**
     * Reports whether the gold crafting interface is on screen.
     *
     * @return true when the interface is drawn and visible.
     */
    public boolean isCraftingInterfaceOpen() {
        WidgetEntity crafting = ctx.widgets().get(InterfaceID.CraftingGold.UNIVERSE);
        return crafting != null && crafting.isVisible();
    }

    @Override
    public int execute() {
        if (isSmelting()) {
            return CRAFTING_POLL_MS;
        }

        if (isCraftingInterfaceOpen()) {
            WidgetEntity necklace = ctx.widgets().get(config.jewelry().getWidgetId());
            if (necklace == null) {
                plugin.halt("The crafting interface has no " + config.jewelry().getNecklaceName() + " to make");
                return 0;
            }

            plugin.moveMouseTo(necklace.raw());
            log.info("Crafting: {}", config.jewelry().makeAction());
            if (!necklace.interact(config.jewelry().makeAction())) {
                log.info("The make action was not available on the {} icon", config.jewelry().getNecklaceName());
                return 600;
            }

            SleepService.sleepUntil(this::isSmelting, RESPONSE_TIMEOUT_MS);
            return 600;
        }

        // The animation stops for a tick between necklaces, so confirm the batch is really over
        // before clicking the furnace again and interrupting it.
        SleepService.tick();
        if (isSmelting()) {
            return CRAFTING_POLL_MS;
        }

        GameObjectEntity furnace = ctx.gameObjects().withId(FURNACE).nearest().orElse(null);
        if (furnace == null) {
            plugin.halt("Standing at the Edgeville furnace with no furnace in the scene");
            return 0;
        }

        plugin.moveMouseTo(furnace.raw());
        if (!furnace.interact("Smelt")) {
            log.info("Smelt action was not available on the furnace at {}", furnace.raw().getWorldLocation());
            return 600;
        }

        SleepService.sleepUntil(this::isCraftingInterfaceOpen, RESPONSE_TIMEOUT_MS);
        return 600;
    }

    /**
     * Reads the player's animation in one client-thread hop.
     *
     * @return true while the furnace animation is playing.
     */
    private boolean isSmelting() {
        return ctx.runOnClientThread(() -> {
            Player local = ctx.getClient().getLocalPlayer();
            return local != null && local.getAnimation() == SMELTING_ANIM;
        }, false);
    }

    @Override
    public String status() {
        return "Crafting Necklaces";
    }
}
