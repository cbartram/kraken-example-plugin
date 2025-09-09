package com.krakenplugins.example.script.actions;

import com.google.inject.Inject;
import com.kraken.api.core.RandomService;
import com.kraken.api.core.SleepService;
import com.kraken.api.core.script.BehaviorResult;
import com.kraken.api.core.script.node.ActionNode;
import com.kraken.api.interaction.gameobject.GameObjectService;
import com.kraken.api.interaction.inventory.InventoryChanged;
import com.kraken.api.interaction.inventory.InventoryService;
import com.kraken.api.interaction.inventory.InventoryUpdateType;
import com.krakenplugins.example.script.BaseScriptNode;
import com.krakenplugins.example.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Skill;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class ClickRockAction extends BaseScriptNode implements ActionNode {

    private final ScriptContext context;
    private final SleepService sleepService;
    private final GameObjectService gameObjectService;

    @Inject
    public ClickRockAction(Client client, ScriptContext context, SleepService sleepService, GameObjectService gameObjectService) {
        super(client, context);
        this.context = context;
        this.sleepService = sleepService;
        this.gameObjectService = gameObjectService;
    }

    @Subscribe
    private void onFakeXpDrop(FakeXpDrop event) {
        log.info("Exp drop in: {}={}", event.getSkill().getName(), event.getXp());
        if(event.getSkill() == Skill.MINING && event.getXp() == 35) {
            log.info("Ore mined");
            context.addOreMined();
        }
    }

    @Override
    public BehaviorResult performAction() {
        context.setStatus("Clicking Rock");
        GameObject nearestRock = gameObjectService.findReachableObject("Iron rocks", true, 5, client.getLocalPlayer().getWorldLocation(), true, "Mine");

        if (nearestRock == null) {
           log.info("No available iron rocks found, waiting for respawn");
           return BehaviorResult.FAILURE;
        }

        context.setTargetRock(nearestRock);

        if (gameObjectService.interact(nearestRock, "Mine")) {
            sleepService.sleepUntil(() -> context.isPlayerMining(client.getLocalPlayer()), RandomService.between(1200, 2000));
            return BehaviorResult.SUCCESS;
        } else {
            context.setTargetRock(null);
            return BehaviorResult.FAILURE;
        }
    }
}
