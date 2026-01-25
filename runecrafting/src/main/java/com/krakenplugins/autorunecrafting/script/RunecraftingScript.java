package com.krakenplugins.autorunecrafting.script;


import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.autorunecrafting.script.task.BankTask;
import com.krakenplugins.autorunecrafting.script.task.OpenBankTask;
import com.krakenplugins.autorunecrafting.script.task.WalkToAltarTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RunecraftingScript extends Script {

    public static final int BANK_BOOTH_ID = 10355;
    public static final int PURE_ESSENCE = 7936;
    public static final int RUNE_ESSENCE = 1436;
    public static final int AIR_TIARA = 5527;

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public RunecraftingScript(BankTask bankTask, OpenBankTask openBankTask, WalkToAltarTask walkToAltarTask) {
        this.tasks = List.of(
            walkToAltarTask,
            openBankTask,
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
