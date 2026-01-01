package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChopLogsTask extends AbstractTask {

    @Inject
    private WoodcuttingPlugin plugin;

    @Inject
    private WoodcuttingConfig config;

    @Inject
    private BankService bankService;

    private int idleTicks = 0;
    private long lastLoopTime = 0;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() && !ctx.inventory().isFull() && !bankService.isOpen();
    }

    @Override
    public int execute() {
        // If it's been more than 2000ms since the last execute call, we assume
        // this is a fresh "idle" session (we just finished chopping), so we reset the counter.
        if (System.currentTimeMillis() - lastLoopTime > 2000) {
            idleTicks = 0;
        }
        lastLoopTime = System.currentTimeMillis();

        // Check if we recently finished dropping (e.g., within 4 seconds)
        // If so, we are "Active" and should click immediately, bypassing the idle simulation.
        long timeSinceDrop = System.currentTimeMillis() - plugin.getLastDropTimestamp();
        boolean isActivePlayer = timeSinceDrop < 4000;

        if (!isActivePlayer) {
            // Calculate a chance to click this tick based on how long we've been waiting.
            // Tick 0: 5% chance (Fast reaction)
            // Tick 1: 10% chance
            // Tick 2: 15% chance
            // ...
            // Tick 20+: 100% chance (Guaranteed to click eventually)
            double reactionChance = 0.02 + (idleTicks * 0.02);

            if (Math.random() > reactionChance) {
                log.info("Missed reaction window, increasing reaction chance by 2% next tick. Current chance = {}", reactionChance);
                idleTicks++;
                return RandomService.between(400, 600);
            }
        }

        GameObjectEntity tree = ctx.gameObjects()
                .within(config.treeRadius())
                .withName(config.treeName()).random();

        if(tree != null) {
            plugin.setTargetTree(tree.raw());
            if(config.useMouse()) {
                ctx.getMouse().move(tree.raw());
            }
            tree.interact("Chop down");
            // Successful click, reset idle ticks for the next time we become idle
            idleTicks = 0;
            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() != -1, RandomService.between(5000, 6000));
        } else {
            log.info("No tree found.");
            idleTicks = 0;
        }

        return 1200;
    }

    @Override
    public String status() {
        if (idleTicks > 0) {
            return "Idling (Reaction Delay)";
        }
        return "Chopping " + config.treeName();
    }
}
