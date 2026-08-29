package com.krakenplugins.autorunecrafting;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.input.mouse.VirtualMouse;
import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.kraken.api.input.mouse.strategy.linear.LinearStrategy;
import com.kraken.api.overlay.GlobalPathfinderOverlay;
import com.kraken.api.overlay.MouseOverlay;
import com.kraken.api.service.tile.AreaService;
import com.kraken.api.service.tile.GameArea;
import com.krakenplugins.autorunecrafting.overlay.SceneOverlay;
import com.krakenplugins.autorunecrafting.overlay.ScriptOverlay;
import com.krakenplugins.autorunecrafting.script.RunecraftingScript;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
@PluginDescriptor(
        name = "Auto Runecrafting",
        enabledByDefault = false,
        description = "Demonstrates an example of building an runecrafting automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken", "runecrafting", "rune"}
)
public class AutoRunecraftingPlugin extends Plugin {

    private static final String CONFIG_GROUP = "autorunecrafting";

    @Inject
    private RunecraftingScript runecraftingScript;

    @Inject
    private Context ctx;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MouseOverlay mouseTrackerOverlay;

    @Inject
    private GlobalPathfinderOverlay pathOverlay;

    @Inject
    private SceneOverlay sceneOverlay;

    @Inject
    private AutoRunecraftingConfig config;

    @Inject
    private AreaService areaService;

    @Inject
    private ScriptOverlay scriptOverlay;

    @Getter
    @Setter
    private volatile GameObject targetBankBooth;

    @Getter
    private volatile GameArea faladorBank;

    @Getter
    private volatile GameArea airAltar;

    private long startTime;

    @Provides
    AutoRunecraftingConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(AutoRunecraftingConfig.class);
    }

    @Override
    protected void startUp() {
        // createPolygonArea rasterizes with java.awt.Polygon insideness rules, which exclude the
        // maximum x and y edges. Both vertex lists are therefore tile corners, not tiles: the bank
        // outline below covers tiles x 3009-3021, y 3353-3356 plus x 3009-3018, y 3357-3358.
        faladorBank = areaService.createPolygonArea(
                new WorldPoint(3009, 3359, 0),
                new WorldPoint(3009, 3353, 0),
                new WorldPoint(3022, 3353, 0),
                new WorldPoint(3022, 3357, 0),
                new WorldPoint(3019, 3357, 0),
                new WorldPoint(3019, 3359, 0)
        );

        airAltar = areaService.createPolygonArea(
                new WorldPoint(2988, 3299, 0),
                new WorldPoint(2976, 3296, 0),
                new WorldPoint(2974, 3288, 0),
                new WorldPoint(2986, 3284, 0),
                new WorldPoint(2996, 3288, 0),
                new WorldPoint(2997, 3296, 0),
                new WorldPoint(2995, 3301, 0),
                new WorldPoint(2989, 3303, 0),
                new WorldPoint(2983, 3302, 0),
                new WorldPoint(2979, 3301, 0),
                new WorldPoint(2976, 3299, 0)
        );

        startTime = System.currentTimeMillis();
        targetBankBooth = null;

        applyMouseConfig();

        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);
        applyPathOverlay();

        if (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
            runecraftingScript.start();
        }
    }

    @Override
    protected void shutDown() {
        runecraftingScript.stop();
        overlayManager.remove(scriptOverlay);
        overlayManager.remove(mouseTrackerOverlay);
        overlayManager.remove(sceneOverlay);
        overlayManager.remove(pathOverlay);
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
            case "showCurrentPath":
                applyPathOverlay();
                break;
            default:
                break;
        }
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        // Covers enabling the plugin while logged out; start() is a no-op when already running
        if (event.getGameState() == GameState.LOGGED_IN) {
            runecraftingScript.start();
        }
    }

    /**
     * Shows or hides the walker's own route overlay, which draws whatever the pathfinder last
     * planned on both the scene and the world map.
     */
    private void applyPathOverlay() {
        if (config.showCurrentPath()) {
            overlayManager.add(pathOverlay);
        } else {
            overlayManager.remove(pathOverlay);
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
        runecraftingScript.pause(reason);
    }

    public String getRuntime() {
        long millis = System.currentTimeMillis() - startTime;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    public String getStatus() {
        return runecraftingScript.getStatus();
    }
}
