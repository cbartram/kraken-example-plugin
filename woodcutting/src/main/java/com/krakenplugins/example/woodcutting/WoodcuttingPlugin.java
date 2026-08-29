package com.krakenplugins.example.woodcutting;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.input.mouse.VirtualMouse;
import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.kraken.api.input.mouse.strategy.linear.LinearStrategy;
import com.kraken.api.overlay.MouseOverlay;
import com.krakenplugins.example.woodcutting.overlay.SceneOverlay;
import com.krakenplugins.example.woodcutting.overlay.ScriptOverlay;
import com.krakenplugins.example.woodcutting.script.WoodcuttingScript;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
@PluginDescriptor(
        name = "Woodcutting Example Plugin",
        enabledByDefault = false,
        description = "Demonstrates an example of building a Woodcutting automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken", "woodcutting"}
)
public class WoodcuttingPlugin extends Plugin {

    private static final String CONFIG_GROUP = "autochopper";

    /** The tile the script chops from, in the willow grove south of the Draynor Village bank. */
    public static final WorldPoint TREE_LOCATION = new WorldPoint(3087, 3233, 0);

    /**
     * How close to {@link #TREE_LOCATION} counts as "at the trees". The walk aims for a random
     * reachable tile inside this radius, so the same value decides both where to stop and whether
     * we have arrived. Small enough that the tree area and the bank never overlap.
     */
    public static final int TREE_AREA_RADIUS = 5;

    @Inject
    private WoodcuttingScript woodcuttingScript;

    @Inject
    private Context ctx;

    @Getter
    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ScriptOverlay scriptOverlay;

    @Inject
    private MouseOverlay mouseTrackerOverlay;

    @Inject
    private SceneOverlay sceneOverlay;

    @Inject
    private WoodcuttingConfig config;

    private long startTime;

    @Getter
    @Setter
    private volatile GameObject targetTree;

    @Getter
    private volatile int logsChopped;

    /** Woodcutting xp at the last {@link StatChanged}, used to tell a real xp drop from the login event. */
    private int lastWoodcuttingXp = -1;

    /**
     * The route the script last walked, for the path overlay. Replaced wholesale rather than mutated,
     * since the overlay iterates it while the script thread is writing.
     */
    @Getter
    @Setter
    private volatile List<WorldPoint> currentPath = List.of();

    @Provides
    WoodcuttingConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(WoodcuttingConfig.class);
    }

    @Override
    protected void startUp() {
        logsChopped = 0;
        lastWoodcuttingXp = -1;
        startTime = System.currentTimeMillis();
        currentPath = List.of();
        targetTree = null;

        applyMouseConfig();

        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);

        if (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
            woodcuttingScript.start();
        }
    }

    @Override
    protected void shutDown() {
        woodcuttingScript.stop();
        overlayManager.remove(scriptOverlay);
        overlayManager.remove(mouseTrackerOverlay);
        overlayManager.remove(sceneOverlay);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(CONFIG_GROUP)) {
            return;
        }

        switch (event.getKey()) {
            case "mouseMovementStrategy":
            case "replayLibrary":
            case "linearSteps":
                applyMouseConfig();
                break;
            default:
                break;
        }
    }

    @Subscribe
    private void onStatChanged(StatChanged event) {
        if (event.getSkill() != Skill.WOODCUTTING) {
            return;
        }

        // The client fires this for every skill on login, so the first event only seeds the baseline.
        if (lastWoodcuttingXp != -1 && event.getXp() > lastWoodcuttingXp) {
            logsChopped++;
        }

        lastWoodcuttingXp = event.getXp();
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        // Covers enabling the plugin while logged out; start() is a no-op when already running.
        if (event.getGameState() == GameState.LOGGED_IN) {
            woodcuttingScript.start();
        }
    }

    private void applyMouseConfig() {
        VirtualMouse.setMouseMovementStrategy(config.mouseMovementStrategy());

        if (config.mouseMovementStrategy() == MouseMovementStrategy.REPLAY) {
            VirtualMouse.loadLibrary(config.replayLibrary());
        }

        if (config.mouseMovementStrategy() == MouseMovementStrategy.LINEAR) {
            LinearStrategy linear = (LinearStrategy) MouseMovementStrategy.LINEAR.getStrategy();
            linear.setSteps(config.linearSteps());
        }
    }

    /**
     * Pauses the script, leaving the reason on the overlay so it is clear why it stopped.
     */
    public void pauseScript(String reason) {
        log.warn("Pausing script: {}", reason);
        woodcuttingScript.pause(reason);
    }

    public String getRuntime() {
        long millis = System.currentTimeMillis() - startTime;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    public String getStatus() {
        return woodcuttingScript.getStatus();
    }
}
