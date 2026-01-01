package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import net.runelite.api.coords.WorldPoint;

import java.util.Map;

public class FindPathTask extends AbstractTask {

    @Inject
    private MovementService movementService;

    @Inject
    private FiremakingConfig config;

    @Override
    public boolean validate() {
        return ctx.inventory().isFull() && ctx.players().local().isIdle() && !isCurrentPositionGood();
    }

    @Override
    public int execute() {
        WorldPoint bestSpot = findBestSpot();
        if (bestSpot != null) {
             if (config.useMouse()) {
                 movementService.moveTo(bestSpot);
             }
             SleepService.sleepUntil(() -> ctx.players().local().raw().getWorldLocation().equals(bestSpot), 8000);
        }
        return 1000;
    }

    private boolean isCurrentPositionGood() {
        // Check if we can burn logs to the West
        // We need as many tiles as we have logs
        long logsCount = ctx.inventory().withName(config.logName()).count();
        return checkLine(ctx.players().local().raw().getWorldLocation(), -1, 0) >= logsCount;
    }

    private WorldPoint findBestSpot() {
        WorldPoint center = ctx.players().local().raw().getWorldLocation();
        WorldPoint best = null;
        int maxLen = -1;
        long logsCount = ctx.inventory().withName(config.logName()).count();

        // Search radius 15
        for (int x = -15; x <= 15; x++) {
            for (int y = -15; y <= 15; y++) {
                WorldPoint p = center.dx(x).dy(y);
                if (ctx.getTileService().isTileReachable(p)) {
                     int len = checkLine(p, -1, 0);
                     if (len >= logsCount) {
                         return p; // Found a good enough spot
                     }
                     if (len > maxLen) {
                         maxLen = len;
                         best = p;
                     }
                }
            }
        }
        return best;
    }

    private int checkLine(WorldPoint start, int dx, int dy) {
        int length = 0;
        WorldPoint current = start;
        // Check up to 28 tiles
        for (int i = 0; i < 28; i++) {
            WorldPoint next = current.dx(dx).dy(dy);
            Map<WorldPoint, Integer> reachable = ctx.getTileService().getReachableTilesFromTile(current, 1, false);
            
            // Check if next is reachable and no fire
            if (reachable.containsKey(next) && ctx.gameObjects().at(next).withName("Fire").first() == null) {
                length++;
                current = next;
            } else {
                break;
            }
        }
        return length;
    }

    @Override
    public String status() {
        return "Finding Path";
    }
}
