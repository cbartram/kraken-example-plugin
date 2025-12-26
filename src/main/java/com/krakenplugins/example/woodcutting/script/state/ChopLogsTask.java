package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
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
    private SleepService sleepService;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle()
                && !ctx.inventory().isFull()
                && ctx.gameObjects().within(8).withName(config.treeName()).random() != null;
    }

    @Override
    public int execute() {
        GameObjectEntity tree = ctx.gameObjects().within(8).withName(config.treeName()).random();

        if(tree != null) {
            plugin.setTargetTree(tree.raw());
            ctx.getMouse().move(tree.raw());
            tree.interact("Chop down");
            sleepService.sleepUntil(() -> ctx.players().local().raw().getAnimation() != -1, RandomService.between(5000, 6000));
        }

        return 0;
    }

    @Override
    public String status() {
        return "Chopping " + config.treeName();
    }
}
