package com.krakenplugins.example.woodcutting.script;


import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.mining.script.state.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WoodcuttingScript extends Script {

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public WoodcuttingScript() {
        this.tasks = List.of();
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
