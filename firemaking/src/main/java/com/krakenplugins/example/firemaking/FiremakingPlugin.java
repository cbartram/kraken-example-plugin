package com.krakenplugins.example.firemaking;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.input.mouse.VirtualMouse;
import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.kraken.api.input.mouse.strategy.linear.LinearStrategy;
import com.kraken.api.overlay.MouseOverlay;
import com.kraken.api.service.tile.AreaService;
import com.kraken.api.service.tile.GameArea;
import com.krakenplugins.example.firemaking.overlay.SceneOverlay;
import com.krakenplugins.example.firemaking.overlay.ScriptOverlay;
import com.krakenplugins.example.firemaking.script.FiremakingScript;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
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
        name = "Firemaking Example Plugin",
        enabledByDefault = false,
        description = "Demonstrates an example of building a Firemaking automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken", "firemaking"}
)
public class FiremakingPlugin extends Plugin {

    private static final String CONFIG_GROUP = "autofiremaker";

    /**
     * How many ticks an animation may run without any firemaking xp before the action it belongs to is
     * treated as stalled. Roughly 10 seconds, which comfortably covers the slowest log burn.
     */
    private static final int STALL_TICKS = 16;

    @Getter
    private volatile GameArea bankLocation;

    @Inject
    private FiremakingScript firemakingScript;

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
    private FiremakingConfig config;

    @Inject
    private AreaService areaService;

    @Getter
    private volatile int logsBurned;

    @Setter
    @Getter
    private volatile NPC targetBanker;

    @Setter
    @Getter
    private volatile GameObject targetFire;

    /**
     * Tick of the last sign of progress: a firemaking xp drop, or an interaction the script just issued.
     */
    private volatile int lastActionTick = -1;

    /** Firemaking xp at the last {@link StatChanged}, used to tell a real xp drop from the login event. */
    private int lastFiremakingXp = -1;

    private long startTime;

    @Provides
    FiremakingConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(FiremakingConfig.class);
    }

    @Override
    protected void startUp() {
        // createPolygonArea rasterizes with java.awt.Polygon insideness rules, which exclude the maximum
        // x and y edges, so the vertices reach one tile past the intended north east corner (3168, 3493).
        bankLocation = areaService.createPolygonArea(List.of(
            new WorldPoint(3161, 3486, 0),
            new WorldPoint(3169, 3486, 0),
            new WorldPoint(3169, 3494, 0),
            new WorldPoint(3161, 3494, 0)
        ));

        logsBurned = 0;
        lastActionTick = -1;
        lastFiremakingXp = -1;
        startTime = System.currentTimeMillis();

        applyMouseConfig();

        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);

        if (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
            firemakingScript.start();
        }
    }

    @Override
    protected void shutDown() {
        firemakingScript.stop();
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
    private void onStatChanged(StatChanged e) {
        if (e.getSkill() != Skill.FIREMAKING) {
            return;
        }

        // The client fires this for every skill on login, so the first event only seeds the baseline.
        if (lastFiremakingXp != -1 && e.getXp() > lastFiremakingXp) {
            logsBurned++;
            markAction();
        }

        lastFiremakingXp = e.getXp();
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        // Covers enabling the plugin while logged out; start() is a no-op when already running.
        if (event.getGameState() == GameState.LOGGED_IN) {
            firemakingScript.start();
        }
    }

    /**
     * Records that something productive just happened, so {@link #isBusy()} keeps waiting on the
     * animation it started.
     */
    public void markAction() {
        lastActionTick = ctx.getClient().getTickCount();
    }

    /**
     * True while the player is doing something the script should not interrupt. Walking always counts.
     * An animation only counts while it is still making progress: once no xp drop or freshly issued
     * interaction has landed for {@link #STALL_TICKS}, the action is treated as stalled so callers can
     * retry it. Deliberately animation-id agnostic, since each log type burns with its own animation.
     */
    public boolean isBusy() {
        if (ctx.players().local().isMoving()) {
            return true;
        }

        if (ctx.players().local().isIdle()) {
            return false;
        }

        final int last = lastActionTick;
        return last != -1 && ctx.getClient().getTickCount() - last <= STALL_TICKS;
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
        firemakingScript.pause(reason);
    }

    public String getRuntime() {
        long millis = System.currentTimeMillis() - startTime;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    public String getStatus() {
        return firemakingScript.getStatus();
    }
}
