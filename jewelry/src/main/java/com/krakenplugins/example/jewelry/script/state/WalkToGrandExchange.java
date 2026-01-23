package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Singleton
@Slf4j
public class WalkToGrandExchange extends AbstractTask {

    private static final WorldPoint GRAND_EXCHANGE = new WorldPoint(3164, 3486, 0);

    @Inject
    private JewelryPlugin plugin;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Inject
    private LocalPathfinder pathfinder;

    @Inject
    private MovementService movementService;

    private List<WorldPoint> currentPath = null;
    private boolean isTraversing = false;

    @Override
    public boolean validate() {
        if(!bankService.isOpen()) {
            return false;
        }

        boolean hasNoGold = ctx.bank().withId(GOLD_BAR).first() == null;
        boolean hasNoGems = ctx.bank().withId(config.jewelry().getSecondaryGemId()).first() == null;

        return ctx.players().local().isInArea(plugin.getEdgevilleBank()) && (hasNoGold || hasNoGems) && !isTraversing;
    }

    @Override
    public int execute() {
        bankService.close();
        try {
            isTraversing = true;
            WorldPoint playerLocation = ctx.getClient().getLocalPlayer().getWorldLocation();

            currentPath = pathfinder.findApproximatePath(playerLocation, GRAND_EXCHANGE);

            if (currentPath == null || currentPath.isEmpty()) {
                log.error("Failed to generate any path to GE");
                isTraversing = false;
                return 1000;
            }

            // Apply variable stride for more natural movement
            List<WorldPoint> stridedPath = movementService.applyVariableStride(currentPath);
            log.info("Path generated with {} waypoints", stridedPath.size());

            // Traverse the path
            boolean success = movementService.traversePath(ctx.getClient(), movementService, stridedPath);

            if (success) {
                log.info("Successfully reached GE");
            } else {
                log.warn("Failed to complete path to GE");
            }

            isTraversing = false;
            return 600;
        } catch (InterruptedException e) {
            log.error("Path traversal interrupted", e);
            isTraversing = false;
            return 1000;
        } catch (Exception e) {
            log.error("Error during walk to GE", e);
            isTraversing = false;
            return 1000;
        }
    }

    @Override
    public String status() {
        return "Walking to G.E.";
    }
}
