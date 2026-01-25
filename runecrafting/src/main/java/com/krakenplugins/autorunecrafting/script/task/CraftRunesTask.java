package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.*;

@Slf4j
@Singleton
public class CraftRunesTask extends AbstractTask {
    @Override
    public boolean validate() {
        GameObjectEntity airAltarInternal = ctx.gameObjects().withId(AIR_ALTAR_INTERNAL).first();
        boolean hasEssence = ctx.inventory().hasItem(PURE_ESSENCE) || ctx.inventory().hasItem(RUNE_ESSENCE);
        return airAltarInternal != null && hasEssence;
    }

    @Override
    public int execute() {
        GameObjectEntity airAltarInternal = ctx.gameObjects().withId(AIR_ALTAR_INTERNAL).first();
        if(airAltarInternal != null) {
            airAltarInternal.interact("Craft-rune");
            SleepService.sleepUntil(() -> !ctx.inventory().hasItem(RUNE_ESSENCE) && !ctx.inventory().hasItem(PURE_ESSENCE));
            GameObjectEntity portal = ctx.gameObjects().withId(PORTAL).first();
            if(portal != null) {
                portal.interact("Use");
                return RandomService.between(2400, 3200);
            }
        } else {
            log.error("No internal air altar could be found");
        }

        return 0;
    }

    @Override
    public String status() {
        return "Crafting Runes";
    }
}
