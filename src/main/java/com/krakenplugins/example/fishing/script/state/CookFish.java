package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CookFish extends AbstractTask {
    private static final int BARBARIAN_VILLAGE_FIRE = 43475;

    @Inject
    private FishingConfig config;

    @Override
    public boolean validate() {
        return ctx.inventory().isFull() && (ctx.inventory().hasItem(335) || ctx.inventory().hasItem(331)) &&
                ctx.players().local().isInArea(FishingLocation.BARBARIAN_VILLAGE.getLocation(), 15) &&
                ctx.players().local().isIdle() &&
                config.barbVillageCook();
    }

    @Override
    public int execute() {
        GameObjectEntity fire = ctx.gameObjects().withId(BARBARIAN_VILLAGE_FIRE).nearest();
        if (fire != null) {
            if (fire.interact("Cook")) {
                // TODO Widgets?
            }
        }
        return 0;
    }

    @Override
    public String status() {
        return "Cooking Fish";
    }
}
