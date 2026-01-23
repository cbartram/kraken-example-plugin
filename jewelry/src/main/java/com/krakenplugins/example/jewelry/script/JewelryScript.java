package com.krakenplugins.example.jewelry.script;

import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.jewelry.script.state.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JewelryScript extends Script {

    public static final int GOLD_BAR = 2357;
    public static final int FURNACE_GAME_OBJECT = 16469;
    public static final int BANK_BOOTH_ID = 10355;

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public JewelryScript(BankTask bankTask, CraftTask craftTask, OpenBankTask openBankTask, OpenFurnaceTask openFurnaceTask,
                         PurchaseSuppliesTask purchaseSuppliesTask, WalkToEdgeville walkToEdgeville, WalkToGrandExchange walkToGrandExchange) {
        this.tasks = List.of(
                openBankTask,
                openFurnaceTask,
                bankTask,
                craftTask,
                purchaseSuppliesTask,
                walkToEdgeville,
                walkToGrandExchange
        );
    }

    @Override
    public int loop() {
        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                return task.execute();
            }
        }
        return 0;
    }
}
