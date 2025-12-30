package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
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

    @Inject
    private JewelryConfig config;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() && ctx.players().local().isInArea(EDGEVILLE_FURNACE, 3) &&
                ctx.inventory().hasItem(GOLD_BAR) && ctx.inventory().hasItem(SAPPHIRE);
    }

    public boolean isCraftingInterfaceOpen() {
        Widget w = ctx.getClient().getWidget(InterfaceID.CraftingGold.UNIVERSE);
        if(w == null) return false;

        return !w.isSelfHidden();
    }

    @Override
    public int execute() {
        for(InventoryEntity e : ctx.inventory().list()) {
            log.info("Item: {} - {}", e.getName(), e.getId());
        }

        log.info(ctx.inventory().hasItem(GOLD_BAR) + " --- " + ctx.inventory().hasItem(SAPPHIRE) + "----" + ctx.inventory().hasItem("Necklace Mould"));


        if (ctx.players().local().raw().getAnimation() == SMELTING_ANIM) {
            log.info("Player SMELTING already, waiting");
            return 600;
        }

        if (isCraftingInterfaceOpen()) {
            // Sapphire: Id	29229080 -> 446.24 or Id 29229078 -> 446.22
            // Option=Make <col=ff9040>Sapphire necklace</col>, Target=, Param0=-1,
            // Param1=29229080, MenuAction=CC_OP, ItemId=1656, id=1, itemOp=-1,
            // str=MenuOptionClicked(getParam0=-1, getParam1=29229080, getMenuOption=Make <col=ff9040>Sapphire necklace</col>, getMenuTarget=, getMenuAction=CC_OP, getId=1)

            SleepService.tick();

            WidgetEntity widget = ctx.widgets().get(29229080);
            if(widget != null && !widget.isNull()) {
                Widget w = widget.raw();
                log.info("Widget Sapphire Necklace: {}, {}, {}, {}", w.getName(), w.getActions(), w.getIndex(), w.getItemId());
                if(config.useMouse()) {
                    ctx.getMouse().move(widget.raw());
                }

                widget.interact("Make <col=ff9040>Sapphire necklace</col>");
            } else {
                log.info("Widget null: {}", widget);
            }

            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() == SMELTING_ANIM, 6000);
            return 600;
        } else {
            log.info("Crafting interface is not open...");
        }

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
