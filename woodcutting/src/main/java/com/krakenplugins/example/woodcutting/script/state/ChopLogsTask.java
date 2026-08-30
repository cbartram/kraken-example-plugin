package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ChopLogsTask extends AbstractTask {

    private static final String CHOP_ACTION = "Chop down";

    /**
     * Consecutive scans that find no choppable tree before the script gives up. Willows regrow within
     * a few ticks and the grove holds several, so this only runs out when the configured tree name
     * does not match anything here.
     */
    private static final int MAX_EMPTY_SCANS = 20;

    @Inject
    private WoodcuttingPlugin plugin;

    @Inject
    private WoodcuttingConfig config;

    private int emptyScans;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() && !ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        // A varying pause before the click, so trees are not lined up the instant the last one falls.
        SleepService.sleep(RandomService.between(200, 1000));

        // Cheap filters first: the reachability and composition lookups only run on what is left.
        // "Chop down" also rules out the same-named farming patch willows, which cannot be chopped.
        GameObjectEntity tree = ctx.gameObjects()
                .within(config.treeRadius())
                .withName(config.treeName())
                .withAction(CHOP_ACTION)
                .reachable()
                .nearest()
                .orElse(null);

        if (tree == null) {
            if (++emptyScans >= MAX_EMPTY_SCANS) {
                plugin.pauseScript("No reachable \"" + config.treeName() + "\" within " + config.treeRadius() + " tiles");
                emptyScans = 0;
            }
            return 1200;
        }

        emptyScans = 0;
        plugin.setTargetTree(tree.raw());

        if (config.useMouse()) {
            ctx.getMouse().move(tree.raw());
        }

        if (!tree.interact(CHOP_ACTION)) {
            log.debug("Chop interaction did not register");
            return 600;
        }

        // Walking to the tree counts: either way the click landed and the player is no longer idle.
        if (!SleepService.sleepUntil(() -> !ctx.players().local().isIdle(), RandomService.between(1200, 1800))) {
            log.debug("Player never started moving or chopping after clicking the tree");
        }

        return 600;
    }

    @Override
    public String status() {
        return "Chopping " + config.treeName();
    }
}
