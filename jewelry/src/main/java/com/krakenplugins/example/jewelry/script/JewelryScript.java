package com.krakenplugins.example.jewelry.script;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.script.state.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

import java.util.List;

@Slf4j
@Singleton
public class JewelryScript extends Script {

    public static final int GOLD_BAR = ItemID.GOLD_BAR;

    // The Edgeville furnace. It is named for the Varrock diary because it shipped with that diary
    // and used to require it; the plain FURNACE object is the one in Al Kharid, Falador and elsewhere.
    public static final int FURNACE = ObjectID.VARROCK_DIARY_FURNACE;

    public static final int BANK_BOOTH = ObjectID.BANKBOOTH;

    private final List<Task> tasks;

    @Inject
    private Context ctx;

    @Inject
    private JewelryConfig config;

    @Getter
    private volatile String status = "Initializing";

    @Inject
    public JewelryScript(EnterBankPinTask enterBankPinTask, BankTask bankTask, CraftTask craftTask, OpenBankTask openBankTask,
                         OpenFurnaceTask openFurnaceTask, PurchaseSuppliesTask purchaseSuppliesTask, WalkToEdgeville walkToEdgeville,
                         WalkToGrandExchange walkToGrandExchange) {
        this.tasks = List.of(
                // Order decides which task wins when several are valid at once. The pin comes first
                // because a pin prompt reads as a closed bank to every other task, which would leave
                // OpenBankTask clicking the booth behind it. The Grand Exchange walk comes before the
                // bank tasks so that an empty bank starts a resupply trip instead of a withdrawal that
                // has nothing to withdraw, and the walk home comes before the purchase so a finished
                // trip leaves the Grand Exchange rather than starting another one.
                enterBankPinTask,
                walkToEdgeville,
                walkToGrandExchange,
                openBankTask,
                openFurnaceTask,
                bankTask,
                craftTask,
                purchaseSuppliesTask
        );
    }

    @Override
    public int loop() {
        enableRun();

        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                // Execute returns the delay required
                return task.execute();
            }
        }

        // Every task is waiting on the game: mid-walk, mid-animation, or an interface still closing.
        status = "Waiting";
        return 0;
    }

    /**
     * Turns run on once energy passes a random point inside the configured band, so the script does
     * not start running at the same percentage on every trip.
     */
    private void enableRun() {
        if (ctx.players().local().isRunEnabled()) {
            return;
        }

        int threshold = RandomService.between(config.runEnergyThresholdMin(), config.runEnergyThresholdMax());
        if (ctx.players().local().currentRunEnergy() >= threshold) {
            log.info("Toggling run on, met threshold: {} between min={} max={}", threshold,
                    config.runEnergyThresholdMin(), config.runEnergyThresholdMax());
            ctx.players().local().toggleRun();
        }
    }
}
