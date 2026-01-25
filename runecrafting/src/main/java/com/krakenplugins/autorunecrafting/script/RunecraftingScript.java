package com.krakenplugins.autorunecrafting.script;


import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RunecraftingScript extends Script {

    public static final int BANK_BOOTH_ID = 10355;

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public RunecraftingScript(BankTask bankTask) {
        this.tasks = List.of(
            bankTask
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
