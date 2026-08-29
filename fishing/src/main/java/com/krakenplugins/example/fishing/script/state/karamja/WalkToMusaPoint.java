package com.krakenplugins.example.fishing.script.state.karamja;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import com.krakenplugins.example.fishing.script.FishingLocation;

import java.util.List;

@Singleton
public class WalkToMusaPoint extends PriorityTask {

    private static final String DESTINATION = "Musa Point";

    /** Inside FishKaramja's fishing radius, so arriving hands straight over to it. */
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
        return config.bankFishKaramja() &&
                !ctx.inventory().isFull() &&
                ctx.inventory().filter(item -> fishIds.contains(item.getId())).count() == 0 &&
                !ctx.players().local().isInArea(FishingLocation.KARAMJA.getLocation(), ARRIVAL_RADIUS);
    }

    @Override
    public int execute() {
        // The return leg of the same trip, ferry included.
        plugin.reportWalk(DESTINATION, walker.walkTo(FishingLocation.KARAMJA.getLocation()));
        return 600;
    }

    @Override
    public String status() {
        return "Walking to Musa Point...";
    }
}
