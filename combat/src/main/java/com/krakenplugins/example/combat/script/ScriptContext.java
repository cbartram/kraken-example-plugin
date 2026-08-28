package com.krakenplugins.example.combat.script;

import com.google.inject.Singleton;
import com.kraken.api.service.util.RandomService;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Singleton
public class ScriptContext {

    private String status = "Initializing";
    private NPC target;
    private List<WorldPoint> reachableTiles = new ArrayList<>();
    private WorldPoint safespot;
    private WorldPoint fightAnchor;
    private long startTimeMillis;
    private int eatThreshold;
    private String haltReason;

    /**
     * Picks a fresh eat threshold at or slightly above the configured value so the script
     * never eats at the same HP percentage twice in a row.
     */
    public void rollEatThreshold(int configuredEatAt) {
        eatThreshold = Math.min(90, configuredEatAt + RandomService.between(0, 8));
    }
}
