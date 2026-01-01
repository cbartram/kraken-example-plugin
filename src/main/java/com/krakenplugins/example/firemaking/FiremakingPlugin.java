package com.krakenplugins.example.firemaking;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.input.mouse.VirtualMouse;
import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.kraken.api.input.mouse.strategy.linear.LinearStrategy;
import com.kraken.api.overlay.MouseOverlay;
import com.krakenplugins.example.firemaking.overlay.SceneOverlay;
import com.krakenplugins.example.firemaking.overlay.ScriptOverlay;
import com.krakenplugins.example.firemaking.script.FiremakingScript;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
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
        name = "Firemaking Example Plugin",
        enabledByDefault = false,
        description = "Demonstrates an example of building a Firemaking automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken", "firemaking"}
)
public class FiremakingPlugin extends Plugin {

    public static final WorldPoint BANK_LOCATION = new WorldPoint(3092, 3243, 0);
    public static final int BANK_BOOTH_GAME_OBJECT = 10355;

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

    private final long startTime = System.currentTimeMillis();

    @Getter
    private int logsBurned;

    @Provides
    FiremakingConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(FiremakingConfig.class);
    }

    @Override
    protected void startUp() {
        ctx.initializePackets();
        firemakingScript.start();
        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);
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
        if(event.getGroup().equals("autofiremaker")) {
            String key = event.getKey();

            if(key.equals("mouseMovementStrategy")) {
                VirtualMouse.setMouseMovementStrategy(config.mouseMovementStrategy());
                if(config.mouseMovementStrategy() == MouseMovementStrategy.REPLAY) {
                    VirtualMouse.loadLibrary(config.replayLibrary());
                }

                if(config.mouseMovementStrategy() == MouseMovementStrategy.LINEAR) {
                    LinearStrategy linear = (LinearStrategy) MouseMovementStrategy.LINEAR.getStrategy();
                    linear.setSteps(config.linearSteps());
                }
            }

        }
    }

    @Subscribe
    private void onFakeXpDrop(FakeXpDrop event) {
        if(event.getSkill() == Skill.FIREMAKING) {
            logsBurned += 1;
        }
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        final GameState gameState = event.getGameState();
        switch (gameState) {
            case LOGGED_IN:
                startUp();
                break;
            case HOPPING:
            case LOGIN_SCREEN:
                shutDown();
            default:
                break;
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
        return firemakingScript.getStatus();
    }
}
