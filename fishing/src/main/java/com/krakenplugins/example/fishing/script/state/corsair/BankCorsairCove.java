package com.krakenplugins.example.fishing.script.state.corsair;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.query.container.bank.DepositBoxEntity;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.DepositBoxService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;

import java.util.List;

@Slf4j
@Singleton
public class BankCorsairCove extends PriorityTask {

    public static final WorldPoint CORSAIR_COVE_DEPOSIT_BOX =  new WorldPoint(2569, 2861, 0);

    /** How close the walker has to leave the player for this task to take over. */
    public static final int DEPOSIT_BOX_RADIUS = 5;

    /** The click walks the player to the box, so the wait has to cover the walk as well as the open. */
    private static final long OPEN_TIMEOUT_MS = 20_000;

    @Inject
    private FishingConfig config;

    @Inject
    private FishingPlugin plugin;

    @Inject
    private DepositBoxService depositBoxService;

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean validate() {
        // Any fish at all rather than a full inventory, so a deposit that only got half of them
        // through finishes the job instead of stranding the script beside the box.
        List<Integer> fishIds = config.fishingLocation().getFishIds();
        return config.bankFishCorsair() &&
                ctx.inventory().filter(item -> fishIds.contains(item.getId())).count() > 0 &&
                ctx.players().local().isIdle() &&
                ctx.players().local().isInArea(CORSAIR_COVE_DEPOSIT_BOX, DEPOSIT_BOX_RADIUS);
    }

    @Override
    public int execute() {
        if(depositBoxService.isOpen()) {
            depositFish();
            return 0;
        }

        GameObjectEntity depositBox = ctx.gameObjects().withId(ObjectID.CORSCURS_BANK_DEPOSIT_BOX).first();
        if(depositBox == null) {
            log.error("No deposit box found...");
            return 600;
        }

        if(config.useMouse()) {
            ctx.getMouse().move(depositBox.raw());
        }

        log.info("Interacting with bank deposit box");
        plugin.setDepositBox(depositBox);
        if (!depositBox.interact("Deposit")) {
            log.error("Deposit box has no Deposit action.");
            plugin.setDepositBox(null);
            return 600;
        }

        if(SleepService.sleepUntilTrue(depositBoxService::isOpen, OPEN_TIMEOUT_MS)) {
            depositFish();
        } else {
            log.warn("Deposit box never opened, retrying.");
        }
        return 600;
    }

    private void depositFish() {
        log.info("Depositing fish from inventory...");
        List<Integer> fishIds = config.fishingLocation().getFishIds();
        List<DepositBoxEntity> fish = ctx.depositBox()
                .inInventory()
                .filter((item) -> fishIds.contains(item.getId()))
                .distinct(DepositBoxEntity::getId)
                .list();

        for(DepositBoxEntity f : fish) {
            if(config.useMouse()) {
                ctx.getMouse().move(f.raw());
            }
            if (!f.depositAll()) {
                log.error("Failed to deposit item id {}", f.getId());
            }
        }

        SleepService.sleepFor(RandomService.between(1, 4));
        depositBoxService.close();
        plugin.setDepositBox(null);
    }

    @Override
    public String status() {
        return "Banking Fish...";
    }
}
