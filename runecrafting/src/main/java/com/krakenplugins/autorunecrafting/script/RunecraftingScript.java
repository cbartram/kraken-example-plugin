package com.krakenplugins.autorunecrafting.script;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.kraken.api.query.player.LocalPlayerEntity;
import com.kraken.api.service.util.RandomService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import com.krakenplugins.autorunecrafting.script.task.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;

import java.util.List;

@Slf4j
@Singleton
public class RunecraftingScript extends Script {

    /**
     * The mysterious ruins that lead into the air temple. No gameval constant, the cache symbol is
     * {@code airtemple_ruined}; it is the multiloc that resolves to the "Enter" variant once a
     * matching tiara is worn.
     */
    public static final int MYSTERIOUS_RUINS = 34813;

    /** The altar inside the air temple. No gameval constant, the cache symbol is {@code air_altar}. */
    public static final int AIR_ALTAR = 34760;

    /** The way back out of the air temple. No gameval constant, the cache symbol is {@code airtemple_exit_portal}. */
    public static final int EXIT_PORTAL = 34748;

    public static final int PURE_ESSENCE = ItemID.BLANKRUNE_HIGH;
    public static final int RUNE_ESSENCE = ItemID.BLANKRUNE;
    public static final int AIR_TIARA = ItemID.TIARA_AIR;

    private final List<Task> tasks;
    private final Context ctx;
    private final AutoRunecraftingConfig config;

    /** Run energy at which run is switched on. Rolled on every start so a config change takes effect. */
    private volatile int runEnergyThreshold;

    @Getter
    private volatile String status = "Initializing";

    @Inject
    public RunecraftingScript(Context ctx, AutoRunecraftingConfig config, BankTask bankTask, WalkToBankTask walkToBankTask, OpenBankTask openBankTask, WalkToAltarTask walkToAltarTask, EnterAltarTask enterAltarTask, CraftRunesTask craftRunesTask) {
        this.ctx = ctx;
        this.config = config;

        // Ordered so that the two walk tasks are the fallback for their half of the trip: anything
        // holding essence that is not already at the ruins walks to them, anything without essence
        // that cannot bank where it stands walks to Falador. That leaves no state uncovered.
        this.tasks = List.of(
            craftRunesTask,
            enterAltarTask,
            bankTask,
            openBankTask,
            walkToAltarTask,
            walkToBankTask
        );
    }

    /**
     * Returns true when the inventory holds essence of either kind, which is the branch every task
     * in this script keys off.
     *
     * @param ctx The game context to read the inventory from.
     * @return True when pure or rune essence is in the inventory.
     */
    public static boolean hasEssence(Context ctx) {
        return ctx.inventory().filter(i -> i.getId() == PURE_ESSENCE || i.getId() == RUNE_ESSENCE).isPresent();
    }

    @Override
    public void onStart() {
        runEnergyThreshold = RandomService.between(config.runEnergyThresholdMin(), config.runEnergyThresholdMax());
        status = "Starting";
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
        LocalPlayerEntity player = ctx.players().local();
        if (player.currentRunEnergy() >= runEnergyThreshold && !player.isRunEnabled()) {
            log.info("Toggling run on, met threshold: {} between min={} max={}", runEnergyThreshold, config.runEnergyThresholdMin(), config.runEnergyThresholdMax());
            player.toggleRun();
        }

        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                return task.execute();
            }
        }
        return 600;
    }
}
