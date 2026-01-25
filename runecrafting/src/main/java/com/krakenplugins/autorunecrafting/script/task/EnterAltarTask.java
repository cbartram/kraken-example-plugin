package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.*;

@Slf4j
@Singleton
public class EnterAltarTask extends AbstractTask {

    @Inject
    private AutoRunecraftingConfig config;

    @Inject
    private AutoRunecraftingPlugin plugin;

    @Override
    public boolean validate() {
        GameObjectEntity altar = ctx.gameObjects().withId(AIR_ALTAR).first();
        boolean hasEssence = ctx.inventory().hasItem(PURE_ESSENCE) || ctx.inventory().hasItem(RUNE_ESSENCE);
        return altar != null && ctx.players().local().isInArea(plugin.getAirAltar()) && hasEssence;
    }

    @Override
    public int execute() {
        GameObjectEntity altar = ctx.gameObjects().withId(AIR_ALTAR).first();
        if(altar != null) {
            if(config.useMouse()) {
                ctx.getMouse().move(altar.raw());
            }
            altar.interact("Enter");
        }
        return RandomService.between(1200, 2400);
    }

    @Override
    public String status() {
        return "Entering Altar";
    }
}
