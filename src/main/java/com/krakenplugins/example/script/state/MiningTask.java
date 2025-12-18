package com.krakenplugins.example.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.MiningPlugin;
import com.krakenplugins.example.script.AbstractTask;
import net.runelite.api.Player;

import static com.krakenplugins.example.MiningPlugin.IRON_ORE_GAME_OBJECT;
import static com.krakenplugins.example.MiningPlugin.MINE_LOCATION;

@Singleton
public class MiningTask extends AbstractTask {

    @Inject
    private SleepService sleepService;

    @Inject
    private MiningPlugin plugin;

    @Override
    public boolean validate() {
        boolean playerInMiningArea = ctx.players().local().isInArea(MINE_LOCATION);
        boolean inventoryHasSpace = !ctx.inventory().isFull();
        boolean isIdle = ctx.players().local().isIdle();

        return playerInMiningArea && inventoryHasSpace && isIdle;
    }

    @Override
    public int execute() {
        GameObjectEntity ironRock = ctx.gameObjects()
                .within(5)
                .withId(IRON_ORE_GAME_OBJECT)
                .random();

        plugin.setTargetRock(ironRock.raw());

        ironRock.interact("Mine");
        if(isPlayerMining(ctx.players().local().raw())) {
            sleepService.sleepUntil(() -> ctx.players().local().isIdle(), 7000);
        }
        return 6400;
    }

    public boolean isPlayerMining(Player player) {
        return player.getAnimation() == net.runelite.api.AnimationID.MINING_BRONZE_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_IRON_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_STEEL_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_BLACK_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_MITHRIL_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_ADAMANT_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_RUNE_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_DRAGON_PICKAXE ||
                player.getAnimation() == net.runelite.api.AnimationID.MINING_CRYSTAL_PICKAXE;
    }

    @Override
    public String status() {
        return "Mining Ore";
    }
}

