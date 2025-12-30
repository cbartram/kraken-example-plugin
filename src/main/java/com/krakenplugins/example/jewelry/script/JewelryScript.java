package com.krakenplugins.example.jewelry.script;

import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.jewelry.script.state.BankTask;
import com.krakenplugins.example.jewelry.script.state.CraftTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JewelryScript extends Script {

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public JewelryScript(BankTask bankTask, CraftTask craftTask) {
        this.tasks = List.of(
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
