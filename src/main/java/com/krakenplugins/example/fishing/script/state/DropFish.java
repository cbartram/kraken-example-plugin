package com.krakenplugins.example.fishing.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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

        log.info("Is Dropping: {}", isDropping);
        return isDropping;
    }

    @Override
    public int execute() {
        List<Integer> fishIds = config.fishingLocation().getFishIds();
        List<InventoryEntity> items = ctx.inventory()
                .orderBy(config.dropPattern())
                .filter(item -> fishIds.contains(item.getId()))
                .list();

        if (items.isEmpty()) {
            isDropping = false;
            return 600;
        }

        // Determine drop count for this specific tick (burst vs single)
        int dropsThisTick = RandomService.between(4, 7);

        for (int i = 0; i < Math.min(items.size(), dropsThisTick); i++) {
            InventoryEntity item = items.get(i);

            if (config.useMouse()) {
                ctx.getMouse().move(item.raw());
            }

            item.drop();
            sleepService.sleep(32, 75);
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