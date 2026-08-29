package com.krakenplugins.example.fishing.script.state.corsair;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;

import java.util.List;

import static com.krakenplugins.example.fishing.script.state.corsair.BankCorsairCove.CORSAIR_COVE_DEPOSIT_BOX;
import static com.krakenplugins.example.fishing.script.state.corsair.BankCorsairCove.DEPOSIT_BOX_RADIUS;

@Singleton
public class WalkToCorsairBank extends PriorityTask {

    private static final String DESTINATION = "the Corsair Cove deposit box";

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
                ctx.inventory().isFull() &&
                ctx.inventory().filter(item -> fishIds.contains(item.getId())).count() > 0 &&
                !ctx.players().local().isInArea(CORSAIR_COVE_DEPOSIT_BOX, DEPOSIT_BOX_RADIUS);
    }

    @Override
    public int execute() {
        // The walker owns the route out of the resource area, including whatever shortcut it needs,
        // and blocks until it arrives or can say why it did not.
        plugin.reportWalk(DESTINATION, walker.walkTo(CORSAIR_COVE_DEPOSIT_BOX));
        return 600;
    }

    @Override
    public String status() {
        return "Walking to Corsair Cove...";
    }
}
