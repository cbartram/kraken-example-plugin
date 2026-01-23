package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class WalkToEdgeville extends AbstractTask {

    private static final WorldPoint EDGEVILLE_BANK = new WorldPoint(3096, 3496, 0);

    @Inject
    private LocalPathfinder pathfinder;

    @Inject
    private MovementService movementService;

    @Inject
    private JewelryPlugin plugin;

    @Inject
    private PurchaseSuppliesTask purchaseSuppliesTask;

    private List<WorldPoint> currentPath = null;
    private boolean isTraversing = false;

    @Override
    public boolean validate() {
        return ctx.players().local().isInArea(plugin.getGrandExchange())
                && !isTraversing
                && purchaseSuppliesTask.isPurchaseComplete();
    }

    @Override
    public int execute() {
        try {
            isTraversing = true;
            WorldPoint playerLocation = ctx.getClient().getLocalPlayer().getWorldLocation();

            currentPath = pathfinder.findApproximatePath(playerLocation, EDGEVILLE_BANK);

            if (currentPath == null || currentPath.isEmpty()) {
                log.error("Failed to generate any path to Edgeville");
                isTraversing = false;
                return 1000;
            }

            // Apply variable stride for more natural movement
            List<WorldPoint> stridedPath = movementService.applyVariableStride(currentPath);
            log.info("Path generated with {} waypoints", stridedPath.size());

            // Traverse the path
            boolean success = movementService.traversePath(ctx.getClient(), movementService, stridedPath);

            if (success) {
                log.info("Successfully reached Edgeville");
            } else {
                log.warn("Failed to complete path to Edgeville");
            }

            isTraversing = false;
            return 600;
        } catch (InterruptedException e) {
            log.error("Path traversal interrupted", e);
            isTraversing = false;
            return 1000;
        } catch (Exception e) {
            log.error("Error during walk to Edgeville", e);
            isTraversing = false;
            return 1000;
        }
    }

    @Override
    public String status() {
        return "Walking to Edgeville";
    }
}