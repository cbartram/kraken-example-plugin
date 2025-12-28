package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;

public class ChopLogsTask extends AbstractTask {

    @Inject
    private WoodcuttingPlugin plugin;

    @Inject
    private WoodcuttingConfig config;

    @Inject
    private BankService bankService;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle()
                && !ctx.inventory().isFull() && !bankService.isOpen();
    }

    @Override
    public int execute() {
        GameObjectEntity tree = ctx.gameObjects()
                .within(config.treeRadius())
                .withName(config.treeName()).random();

        if(tree != null) {
            plugin.setTargetTree(tree.raw());
            if(config.useMouse()) {
                ctx.getMouse().move(tree.raw());
            }
            tree.interact("Chop down");
            SleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() != -1, RandomService.between(5000, 6000));
        }

        return 1200;
    }

    @Override
    public String status() {
        return "Chopping " + config.treeName();
    }
}
