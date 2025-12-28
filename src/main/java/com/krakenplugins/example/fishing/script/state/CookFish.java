package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.query.widget.WidgetEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
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

    // TODO ResumePause Packet must be sent here, not a widget menu action click. Need to also sleep while players animation is cooking
    // there is a small delay where players animation is not cooking before they cook the next item so need to have a buffer to account
    // for this.
    @Override
    public int execute() {
        GameObjectEntity fire = ctx.gameObjects().withId(BARBARIAN_VILLAGE_FIRE).nearest();
        if (fire != null) {
            if (fire.interact("Cook")) {
                SleepService.sleepUntilTick(RandomService.between(3, 4));
                WidgetEntity cookOne = ctx.widgets().withId(COOK_WIDGET_ONE).nameContains("<col=ff9040>Trout</col>").first();
                WidgetEntity cookTwo = ctx.widgets().withId(COOK_WIDGET_TWO).nameContains("<col=ff9040>Trout</col>").first();

                if(cookTwo != null) {
                   if(cookTwo.getName().toLowerCase().contains("trout")) {
                       cookTwo.interact("Cook");
                   }
               }

               if(cookOne != null) {
                   String name = cookOne.getName();
                   if(name.toLowerCase().contains("trout")) {
                       cookOne.interact("Cook");
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

    // Cooking gets priority over dropping when applicable
    @Override
    public int getPriority() {
        return 100;
    }
}
