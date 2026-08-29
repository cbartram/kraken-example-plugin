package com.krakenplugins.example.woodcutting.script;


import com.google.inject.Inject;
import com.google.inject.Singleton;
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
@Singleton
public class WoodcuttingScript extends Script {

    private final List<Task> tasks;

    @Getter
    private volatile String status = "Initializing";

    @Inject
    public WoodcuttingScript(EnterBankPinTask enterBankPinTask, DepositLogsTask depositLogsTask, BankTask bankTask,
                             WalkToTrees walkToTrees, ChopLogsTask chopLogsTask) {
        // Order matters: only the first task whose validate() passes runs each loop. An open interface
        // is always dealt with first so the script can never park behind one, and the walk back to the
        // trees is checked before chopping so a full inventory dropped off at the bank does not leave
        // the script hunting for trees it cannot see.
        this.tasks = List.of(
                enterBankPinTask,
                depositLogsTask,
                bankTask,
                walkToTrees,
                chopLogsTask
        );
    }

    /**
     * Pauses the loop and leaves the reason on the overlay in place of the current task's status.
     */
    public void pause(String reason) {
        status = reason;
        pause();
    }

    @Override
    public int loop() {
        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                return task.execute();
            }
        }

        // Nothing to do this tick: the player is chopping, walking, or otherwise busy.
        return 600;
    }
}
