package com.krakenplugins.example.firemaking.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FindPathTask extends AbstractTask {

    @Inject
    private MovementService movementService;

    @Inject
    private FiremakingConfig config;

    @Override
    public boolean validate() {
        // Run if inventory is full of logs
        // OR we have logs but cannot burn in the current position (West blocked)
        boolean hasLogs = !ctx.inventory().withName(config.logName()).isEmpty();
        return hasLogs && ctx.players().local().isIdle() && !canBurnWest();
    }

    @Override
    public int execute() {
        WorldPoint bestSpot = findBestSpot();
        if (bestSpot != null) {
            if (config.useMouse()) {
                ctx.getMouse().move(bestSpot);
            }
            movementService.moveTo(bestSpot);

            SleepService.sleepUntil(() ->
                    ctx.players().local().raw().getWorldLocation().distanceTo(bestSpot) <= 1, 8000);
        }
        return 600;
    }

    /**
     * Checks if the IMMEDIATE tile to the West is valid.
     * If this returns false, the Task validates true, prompting a move.
     */
    private boolean canBurnWest() {
        WorldPoint current = ctx.players().local().raw().getWorldLocation();
        // Check current tile for fire (cannot light fire on fire)
        if (isTileBlockedOrHasFire(current)) return false;

        // Check immediate west tile
        return !isTileBlockedOrHasFire(current.dx(-1));
    }

    public WorldPoint findBestSpot() {
        WorldPoint center = ctx.players().local().raw().getWorldLocation();
        List<FireLine> validLines = new ArrayList<>();
        int searchRadius = 15;

        for (int y = -searchRadius; y <= searchRadius; y++) {
            int currentStripLength = 0;
            WorldPoint stripStart = null;

            // Iterate West to East (or East to West) across the row
            // We scan a bit wider (20) to find full lines
            for (int x = 20; x >= -20; x--) {
                WorldPoint p = center.dx(x).dy(y);

                // If tile is free of obstacles and fires
                if (!isTileBlockedOrHasFire(p)) {
                    if (currentStripLength == 0) {
                        stripStart = p;
                    }
                    currentStripLength++;
                } else {
                    // Strip ended (hit a wall or fire)
                    if (currentStripLength > 0) {
                        // Create a line starting at the Eastern-most point of the strip
                        // Because we burn West, the start point is the generic 'stripStart'
                        validLines.add(new FireLine(stripStart, currentStripLength, center.distanceTo(stripStart)));
                    }
                    currentStripLength = 0;
                    stripStart = null;
                }
            }
        }

        // 2. Sort lines: Prefer Longest lines, then Closest lines
        // We only care if the line is at least 3 tiles long to be worth moving
        return validLines.stream()
                .filter(line -> line.length >= 3)
                .sorted(Comparator.comparingInt(FireLine::getLength).reversed()
                        .thenComparingInt(FireLine::getDistance))
                .filter(line -> ctx.getTileService().isTileReachable(line.start)) // Expensive check happens LAST on few candidates
                .map(FireLine::getStart)
                .findFirst()
                .orElse(null);
    }

    @Getter
    @AllArgsConstructor
    private static class FireLine {
        WorldPoint start;
        int length;
        int distance;
    }

    private boolean isTileBlockedOrHasFire(WorldPoint p) {
        boolean hasFire = ctx.gameObjects().at(p).withName("Fire").first() != null;
        if (hasFire) return true;

        return !ctx.getTileService().isTileReachable(p);
    }

    @Override
    public String status() {
        return "Finding new Fire Line";
    }
}