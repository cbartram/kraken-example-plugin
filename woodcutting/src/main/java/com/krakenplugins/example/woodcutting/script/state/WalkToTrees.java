package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.movement.MovementService;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.TREE_AREA_RADIUS;
import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.TREE_LOCATION;

@Slf4j
@Singleton
public class WalkToTrees extends AbstractTask {

    /** Long enough to cross the scene on foot, after which the walk is reissued. */
    private static final int WALK_TIMEOUT_MS = 15000;

    @Inject
    private LocalPathfinder pathfinder;

    @Inject
    private MovementService movementService;

    @Inject
    private WoodcuttingPlugin plugin;

    @Override
    public boolean validate() {
        // Anywhere but the grove with room left to chop means walking back, whether that is the bank
        // after a deposit or wherever the plugin happened to be enabled.
        return !ctx.inventory().isFull()
                && !ctx.players().local().isInArea(TREE_LOCATION, TREE_AREA_RADIUS);
    }

    @Override
    public int execute() {
        List<WorldPoint> path = pathfinder.findApproximatePath(ctx.players().local().location(), TREE_LOCATION, TREE_AREA_RADIUS);

        if (path == null || path.isEmpty()) {
            plugin.pauseScript("No path to the trees from here");
            return 600;
        }

        plugin.setCurrentPath(path);

        // The whole route is inside the loaded scene, so one click on the last tile walks it.
        movementService.moveTo(path.get(path.size() - 1));

        if (!SleepService.sleepUntil(() -> ctx.players().local().isInArea(TREE_LOCATION, TREE_AREA_RADIUS), WALK_TIMEOUT_MS)) {
            log.debug("Did not reach the trees within {}ms", WALK_TIMEOUT_MS);
        }

        return 600;
    }

    @Override
    public String status() {
        return "Walking to Trees";
    }
}
