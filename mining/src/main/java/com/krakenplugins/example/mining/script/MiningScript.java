package com.krakenplugins.example.mining.script;

import com.google.inject.Inject;
import com.kraken.api.Context;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.example.mining.MiningConfig;
import com.krakenplugins.example.mining.script.state.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MiningScript extends Script {

    private final List<Task> tasks;

    @Inject
    private Context ctx;

    @Inject
    private MiningConfig config;

    @Getter
    private volatile String status = "Initializing";

    @Inject
    public MiningScript(
            MiningTask miningTask,
            BankingTask bankingTask,
            OpenBankTask openBankTask,
            WalkToMineTask walkToMineTask,
            WalkToBankTask walkToBankTask
    ) {
        this.tasks = List.of(
                miningTask,
                bankingTask,
                openBankTask,
                walkToMineTask,
                walkToBankTask
        );
    }

    @Override
    public int loop() {
        enableRun();

        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                // Execute returns the delay required
                return task.execute();
            }
        }

        // Every task is waiting on the game: mid-animation, mid-walk, or an interface still closing.
        status = "Waiting";
        return 0;
    }

    /**
     * Turns run on once energy passes a random point inside the configured band, so the script does
     * not start running at the same percentage on every trip.
     */
    private void enableRun() {
        if (ctx.players().local().isRunEnabled()) {
            return;
        }

        int threshold = RandomService.between(config.runEnergyThresholdMin(), config.runEnergyThresholdMax());
        if (ctx.players().local().currentRunEnergy() >= threshold) {
            log.info("Toggling run on, met threshold: {} between min={} max={}", threshold,
                    config.runEnergyThresholdMin(), config.runEnergyThresholdMax());
            ctx.players().local().toggleRun();
        }
    }
}
