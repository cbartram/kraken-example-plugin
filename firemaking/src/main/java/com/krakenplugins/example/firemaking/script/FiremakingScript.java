package com.krakenplugins.example.firemaking.script;

import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.firemaking.script.state.BankTask;
import com.krakenplugins.example.firemaking.script.state.BurnLogsTask;
import com.krakenplugins.example.firemaking.script.state.EnterBankPinTask;
import com.krakenplugins.example.firemaking.script.state.WithdrawLogsTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FiremakingScript extends Script {

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public FiremakingScript(EnterBankPinTask pin, BankTask bankTask, WithdrawLogsTask withdrawLogsTask, BurnLogsTask burnLogsTask) {
        this.tasks = List.of(
                pin,
                bankTask,
                withdrawLogsTask,
                burnLogsTask
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
