package com.krakenplugins.example.jewelry.script;

import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.kraken.api.service.tile.AreaService;
import com.krakenplugins.example.jewelry.script.state.BankTask;
import com.krakenplugins.example.jewelry.script.state.CraftTask;
import com.krakenplugins.example.jewelry.script.state.OpenBankTask;
import com.krakenplugins.example.jewelry.script.state.OpenFurnaceTask;
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
    public JewelryScript(BankTask bankTask, CraftTask craftTask, OpenBankTask openBankTask, OpenFurnaceTask openFurnaceTask, AreaService areaService) {
        this.tasks = List.of(
                openBankTask,
                openFurnaceTask,
                bankTask,
                craftTask
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
