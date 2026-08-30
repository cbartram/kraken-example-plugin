package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.*;

@Slf4j
@Singleton
public class CraftRunesTask extends AbstractTask {

    /** Crafting a whole inventory is one action, but the essence only clears once the server replies. */
    private static final long CRAFT_TIMEOUT_MS = 5_000L;

    @Inject
    private AutoRunecraftingConfig config;

    @Override
    public boolean validate() {
        // Seeing the altar is the only proof that we are inside the temple, and it is what puts this
        // task ahead of the walk tasks: the temple is nowhere near either of their destinations.
        return ctx.gameObjects().withId(AIR_ALTAR).isPresent();
    }

    @Override
    public int execute() {
        if (hasEssence(ctx)) {
            GameObjectEntity altar = ctx.gameObjects().withId(AIR_ALTAR).first().orElse(null);
            if (altar == null) {
                return 600;
            }

            if (config.useMouse()) {
                ctx.getMouse().move(altar.raw());
            }

            if (!altar.interact("Craft-rune")) {
                log.warn("Craft-rune was not available on the altar");
                return 600;
            }

            // Leaving with essence still in the inventory only means walking straight back in, so
            // stay put until the craft has actually landed.
            if (!SleepService.sleepUntil(() -> !hasEssence(ctx), CRAFT_TIMEOUT_MS)) {
                log.warn("Essence was still in the inventory after crafting");
                return 600;
            }
        }

        GameObjectEntity portal = ctx.gameObjects().withId(EXIT_PORTAL).first().orElse(null);
        if (portal == null) {
            log.warn("Exit portal not found inside the air temple");
            return 600;
        }

        if (config.useMouse()) {
            ctx.getMouse().move(portal.raw());
        }

        portal.interact("Use");
        return RandomService.between(2400, 3200);
    }

    @Override
    public String status() {
        return "Crafting Runes";
    }
}
