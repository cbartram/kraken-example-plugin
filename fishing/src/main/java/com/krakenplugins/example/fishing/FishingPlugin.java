package com.krakenplugins.example.fishing;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.input.mouse.VirtualMouse;
import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.kraken.api.input.mouse.strategy.linear.LinearStrategy;
import com.kraken.api.overlay.GlobalPathfinderOverlay;
import com.kraken.api.overlay.MouseOverlay;
import com.kraken.api.overlay.log.PluginLogger;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.walker.WalkOutcome;
import com.kraken.api.service.walker.WalkResult;
import com.krakenplugins.example.fishing.overlay.SceneOverlay;
import com.krakenplugins.example.fishing.overlay.ScriptOverlay;
import com.krakenplugins.example.fishing.script.FishingScript;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
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
        name = "Fishing Example Plugin",
        enabledByDefault = false,
        description = "Demonstrates an example of building a fishing automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken", "fishing"}
)
public class FishingPlugin extends Plugin {

    private static final String PLUGIN_PACKAGE = "com.krakenplugins.example.fishing";
    private static final String CONFIG_GROUP = "autofisher";

    @Inject
    private FishingScript fishingScript;

    @Inject
    private Context ctx;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ScriptOverlay scriptOverlay;

    @Inject
    private MouseOverlay mouseTrackerOverlay;

    @Inject
    private GlobalPathfinderOverlay globalPathfinderOverlay;

    @Inject
    private SceneOverlay sceneOverlay;

    @Inject
    private FishingConfig config;

    @Inject
    private PluginLogger pluginLogger;

    private long startTime;

    @Getter
    @Setter
    private volatile NpcEntity targetSpot;

    @Getter
    @Setter
    private volatile GameObjectEntity depositBox;

    @Getter
    private volatile int fishCaught;

    /** Fishing xp at the last {@link StatChanged}, used to tell a real xp drop from the login event. */
    private int lastFishingXp = -1;

    /** When the last inventory of fish finished dropping. Written and read on the script thread. */
    @Getter
    @Setter
    private long lastDropTimestamp;

    @Provides
    FishingConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(FishingConfig.class);
    }

    @Override
    protected void startUp() {
        fishCaught = 0;
        lastFishingXp = -1;
        lastDropTimestamp = 0;
        startTime = System.currentTimeMillis();

        fishingScript.setTasksForLocation(config.fishingLocation());
        applyMouseConfig();
        pluginLogger.attach(PLUGIN_PACKAGE);

        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);
        if (config.highlightCurrentPath()) {
            overlayManager.add(globalPathfinderOverlay);
        }

        if (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
            fishingScript.start();
        }
    }

    @Override
    protected void shutDown() {
        fishingScript.stop();
        pluginLogger.detach();

        overlayManager.remove(scriptOverlay);
        overlayManager.remove(mouseTrackerOverlay);
        overlayManager.remove(sceneOverlay);
        overlayManager.remove(globalPathfinderOverlay);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(CONFIG_GROUP)) {
            return;
        }

        switch (event.getKey()) {
            case "fishingLocation":
            case "dropFish":
                log.info("Updating fishing location to: {}", config.fishingLocation().name());
                fishingScript.setTasksForLocation(config.fishingLocation());
                break;
            case "mouseMovementStrategy":
            case "replayLibrary":
            case "linearSteps":
                applyMouseConfig();
                break;
            case "highlightCurrentPath":
                if (config.highlightCurrentPath()) {
                    overlayManager.add(globalPathfinderOverlay);
                } else {
                    overlayManager.remove(globalPathfinderOverlay);
                }
                break;
            default:
                break;
        }
    }

    @Subscribe
    private void onStatChanged(StatChanged event) {
        if (event.getSkill() != Skill.FISHING) {
            return;
        }

        // The client fires this for every skill on login, so the first event only seeds the baseline.
        if (lastFishingXp != -1 && event.getXp() > lastFishingXp) {
            fishCaught++;
        }

        lastFishingXp = event.getXp();
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        // Covers enabling the plugin while logged out; start() is a no-op when already running.
        if (event.getGameState() == GameState.LOGGED_IN) {
            fishingScript.start();
        }
    }

    /**
     * Pauses the script, leaving the reason on the overlay so it is clear why it stopped.
     */
    public void pauseScript(String reason) {
        log.warn("Pausing script: {}", reason);
        fishingScript.pause(reason);
    }

    /**
     * Reports how a walk ended. A stall or a timeout is worth another attempt on the next loop, so
     * those only log. Anything else -- no route, or a transport the walker cannot operate or pay for --
     * will not fix itself, so the script stops with the walker's own reason on the overlay.
     *
     * @param destination what the walk was heading for, used in the message
     * @param result what the walker reported
     */
    public void reportWalk(String destination, WalkResult result) {
        if (result.isSuccess()) {
            return;
        }

        log.warn("Walk to {} ended as {}", destination, result);
        if (result.getOutcome() != WalkOutcome.STALLED && result.getOutcome() != WalkOutcome.TIMED_OUT) {
            pauseScript("Cannot reach " + destination + ": " + result.getReason());
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

    public String getRuntime() {
        long millis = System.currentTimeMillis() - startTime;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    public String getStatus() {
        return fishingScript.getStatus();
    }
}
