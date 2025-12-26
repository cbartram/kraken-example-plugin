package com.krakenplugins.example.mining.script;

import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.mining.script.state.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MiningScript extends Script {

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public MiningScript(
            FollowPathTask followPathTask,
            MiningTask miningTask,
            BankingTask bankingTask,
            OpenBankTask openBankTask,
            WalkToMineTask walkToMineTask,
            WalkToBankTask walkToBankTask
    ) {
        this.tasks = List.of(
                followPathTask,
                miningTask,
                bankingTask,
                openBankTask,
                walkToMineTask,
                walkToBankTask
        );
    }

    @Override
    public int loop() {
        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                // Execute returns the delay required
                return task.execute();
            }
        }
        return 0;
    }
}