package com.krakenplugins.example.fishing.script.state.karamja;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.service.walker.Walker;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;

import java.util.List;

import static com.krakenplugins.example.fishing.script.state.karamja.BankDepositBox.PORT_SARIM_DEPOSIT_BOX;
import static com.krakenplugins.example.fishing.script.state.karamja.BankDepositBox.DEPOSIT_BOX_RADIUS;

@Singleton
public class TravelPortSarim extends PriorityTask {

    private static final String DESTINATION = "the Port Sarim deposit box";

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
                ctx.inventory().isFull() &&
                ctx.inventory().filter(item -> fishIds.contains(item.getId())).count() > 0 &&
                !ctx.players().local().isInArea(PORT_SARIM_DEPOSIT_BOX, DEPOSIT_BOX_RADIUS);
    }

    @Override
    public int execute() {
        // One call covers the walk to the Karamja dock, the ferry, and the walk to the box on the
        // other side. The walker resolves whichever variant of the sailor is standing there and works
        // through the fare dialogue, and it blocks until it arrives or can say why it did not.
        plugin.reportWalk(DESTINATION, walker.walkTo(PORT_SARIM_DEPOSIT_BOX));
        return 600;
    }

    @Override
    public String status() {
        return "Traveling to Port Sarim...";
    }
}
