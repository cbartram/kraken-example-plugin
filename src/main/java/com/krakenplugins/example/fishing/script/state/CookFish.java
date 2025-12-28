package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.script.FishingLocation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CookFish extends PriorityTask {
    private static final int BARBARIAN_VILLAGE_FIRE = 43475;
    private static final int COOK_WIDGET_ONE = 17694735;
    private static final int COOK_WIDGET_TWO = 17694736;

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
                WidgetEntity cookOne = ctx.widgets().withId(17694735).first();
                WidgetEntity cookTwo = ctx.widgets().withId(17694736).first();
               if(cookTwo != null) {
                   log.info("Found COOK_TWO widget");
                   String name = cookTwo.getName();
                   if(name.toLowerCase().contains("trout")) {

                   }

               }
            }
        }
        return 0;
    }

    @Override
    public String status() {
        return "Cooking Fish";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
