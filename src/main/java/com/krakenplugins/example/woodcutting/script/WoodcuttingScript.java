package com.krakenplugins.example.woodcutting.script;


import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.woodcutting.script.state.BankTask;
import com.krakenplugins.example.woodcutting.script.state.ChopLogsTask;
import com.krakenplugins.example.woodcutting.script.state.DepositLogsTask;
import com.krakenplugins.example.woodcutting.script.state.EnterBankPinTask;
import com.krakenplugins.example.woodcutting.script.state.WalkToTrees;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WoodcuttingScript extends Script {

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public WoodcuttingScript(BankTask bankTask, ChopLogsTask chopLogsTask, DepositLogsTask depositLogsTask, WalkToTrees walkToTrees, EnterBankPinTask enterBankPinTask) {
        this.tasks = List.of(
                enterBankPinTask,
                chopLogsTask,
                bankTask,
                walkToTrees,
                depositLogsTask
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
