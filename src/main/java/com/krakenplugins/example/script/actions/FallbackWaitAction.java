package com.krakenplugins.example.script.actions;

import com.google.inject.Inject;
import com.kraken.api.core.SleepService;
import com.kraken.api.core.script.BehaviorResult;
import com.kraken.api.core.script.node.ActionNode;
import com.kraken.api.interaction.player.PlayerService;
import com.kraken.api.util.RandomUtils;
import com.krakenplugins.example.script.BaseScriptNode;
import com.krakenplugins.example.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

@Slf4j
public class FallbackWaitAction extends BaseScriptNode implements ActionNode {

    private final SleepService sleepService;
    private final PlayerService playerService;
    private int selectedEnergyPoint = -1;

    @Inject
    public FallbackWaitAction(Client client, ScriptContext context, SleepService sleepService, PlayerService playerService) {
        super(client, context);
        this.sleepService = sleepService;
        this.playerService = playerService;
    }

    @Override
    public BehaviorResult performAction() {
        // Since this happens during transitions between the bank and mining area it makes sense to check
        // the players run energy here and re-enable it.
        if(selectedEnergyPoint == -1) {
            selectedEnergyPoint = RandomUtils.randomIntBetween(50, 99);
        }

        if(!playerService.isRunEnabled() && playerService.currentRunEnergy() >= selectedEnergyPoint) {
            log.info("Activating run. Current energy: {} and selected point is: {}", playerService.currentRunEnergy(), selectedEnergyPoint);
            playerService.activateRun();
            selectedEnergyPoint = -1;
        }

        context.setStatus("Waiting...");
        sleepService.sleep(100, 200);
        return BehaviorResult.SUCCESS;
    }
}
