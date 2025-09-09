package com.krakenplugins.example.script;

import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.AnimationID;
import net.runelite.api.Player;
import net.runelite.api.TileObject;

@Getter
@Setter
@Singleton
public class ScriptContext {
    private TileObject targetRock = null;
    private int oreMined = 0;
    private String status = "";
    private String runtime = "";
    
    public boolean isPlayerMining(Player player) {
        return player.getAnimation() == AnimationID.MINING_IRON_PICKAXE ||
                player.getAnimation() == AnimationID.MINING_STEEL_PICKAXE ||
                player.getAnimation() == AnimationID.MINING_MITHRIL_PICKAXE ||
                player.getAnimation() == AnimationID.MINING_ADAMANT_PICKAXE ||
                player.getAnimation() == AnimationID.MINING_RUNE_PICKAXE ||
                player.getAnimation() == AnimationID.MINING_DRAGON_PICKAXE;
    }

    public void addOreMined() {
        this.oreMined += 1;
    }
}