package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;

import java.util.List;

public class DropFish extends AbstractTask {

    @Inject
    private FishingConfig config;

    @Inject
    private SleepService sleepService;

    // LATCH: State memory to persist "Dropping" mode
    private boolean isDropping = false;

    @Override
    public boolean validate() {
        boolean isFull = ctx.inventory().isFull();

        List<Integer> fishIds = config.fishingLocation().getFishIds();
        boolean hasFish = ctx.inventory().filter(item -> fishIds.contains(item.getId())).count() > 0;

        // Engage if full. Disengage ONLY if no fish left.
        if (isFull) {
            isDropping = true;
        } else if (!hasFish) {
            isDropping = false;
        }

        return isDropping;
    }

    @Override
    public int execute() {
        // Ban Safety: Don't use a loop. Drop 1-2 items per script cycle (per tick).
        // This creates a natural rhythm and allows the script to react to other events.
        List<Integer> fishIds = config.fishingLocation().getFishIds();
        List<InventoryEntity> items = ctx.inventory()
                .filter(item -> fishIds.contains(item.getId()))
                .list();

        if (items.isEmpty()) return 600;

        // Determine drop count for this specific tick (burst vs single)
        int dropsThisTick = RandomService.between(3, 6);

        for (int i = 0; i < Math.min(items.size(), dropsThisTick); i++) {
            InventoryEntity item = items.get(i);

            if (config.useMouse()) {
                ctx.getMouse().move(item.raw());
            }

            item.drop();

            sleepService.sleep(50, 90);
        }

        // Return delay for next loop.
        // 600ms is 1 tick. Returning slightly less (e.g., 400-600) ensures we catch the next tick.
        return RandomService.between(400, 600);
    }

    @Override
    public String status() {
        return "Dropping items";
    }
}