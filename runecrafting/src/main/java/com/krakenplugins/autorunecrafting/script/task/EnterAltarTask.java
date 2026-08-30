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
        // The ruins are visible from most of the scene, so the area check is what separates "standing
        // at the ruins" from "still walking towards them". Cheapest checks first, scene scan last.
        return ctx.players().local().isInArea(plugin.getAirAltar())
                && hasEssence(ctx)
                && ctx.gameObjects().withId(MYSTERIOUS_RUINS).isPresent();
    }

    @Override
    public int execute() {
        GameObjectEntity ruins = ctx.gameObjects().withId(MYSTERIOUS_RUINS).first().orElse(null);
        if (ruins == null) {
            return 600;
        }

        if (config.useMouse()) {
            ctx.getMouse().move(ruins.raw());
        }

        // The ruins only offer "Enter" while a matching tiara is worn, so a missing action means the
        // tiara is missing rather than the click being mistimed.
        if (!ruins.interact("Enter")) {
            plugin.pauseScript("Could not enter the ruins, an air tiara must be equipped");
            return 600;
        }

        return RandomService.between(1200, 2400);
    }

    @Override
    public String status() {
        return "Entering Altar";
    }
}
