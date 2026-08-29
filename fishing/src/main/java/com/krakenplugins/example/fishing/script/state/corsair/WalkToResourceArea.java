package com.krakenplugins.example.fishing.script.state.corsair;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import com.krakenplugins.example.fishing.script.FishingLocation;

import java.util.List;

@Singleton
public class WalkToResourceArea extends PriorityTask {

    private static final String DESTINATION = "the Corsair Cove Resource Area";

    /** Inside FishCorsair's fishing radius, so arriving hands straight over to it. */
    private static final int ARRIVAL_RADIUS = 8;

    @Inject
    private FishingConfig config;

    @Inject
    private FishingPlugin plugin;

    @Inject
    private Walker walker;

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean validate() {
        List<Integer> fishIds = config.fishingLocation().getFishIds();
        return config.bankFishCorsair() &&
                !ctx.inventory().isFull() &&
                ctx.inventory().filter(item -> fishIds.contains(item.getId())).count() == 0 &&
                !ctx.players().local().isInArea(FishingLocation.CORSAIR_COVE.getLocation(), ARRIVAL_RADIUS);
    }

    @Override
    public int execute() {
        // The return leg of the same trip.
        plugin.reportWalk(DESTINATION, walker.walkTo(FishingLocation.CORSAIR_COVE.getLocation()));
        return 600;
    }

    @Override
    public String status() {
        return "Walking to the Corsair Cove Resource Area...";
    }
}
