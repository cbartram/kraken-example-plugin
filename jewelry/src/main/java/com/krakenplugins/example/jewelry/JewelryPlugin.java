package com.krakenplugins.example.jewelry;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.input.mouse.VirtualMouse;
import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.kraken.api.input.mouse.strategy.linear.LinearStrategy;
import com.kraken.api.overlay.MouseOverlay;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.tile.AreaService;
import com.kraken.api.service.tile.GameArea;
import com.krakenplugins.example.jewelry.overlay.SceneOverlay;
import com.krakenplugins.example.jewelry.overlay.ScriptOverlay;
import com.krakenplugins.example.jewelry.script.JewelryScript;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
@PluginDescriptor(
        name = "Jewelry Example Plugin",
        enabledByDefault = false,
        description = "Demonstrates an example of building a Jewelry automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken", "jewelry"}
)
public class JewelryPlugin extends Plugin {

    @Inject
    private JewelryScript jewelryScript;

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
    private JewelryConfig config;

    @Inject
    private AreaService areaService;

    @Getter
    @Setter
    private GameObjectEntity targetBankBooth;

    @Getter
    private GameArea edgevilleFurnace;

    @Getter
    private GameArea edgevilleBank;

    @Getter
    private GameArea grandExchange;

    private final long startTime = System.currentTimeMillis();

    @Provides
    JewelryConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(JewelryConfig.class);
    }

    @Override
    protected void startUp() {
        ctx.initializePackets();

        WorldPoint[] furnace = {
                new WorldPoint(3105, 3502, 0),
                new WorldPoint(3105, 3496, 0),
                new WorldPoint(3111, 3496, 0),
                new WorldPoint(3111, 3502, 0)
        };
        edgevilleFurnace = areaService.createPolygonArea(Arrays.asList(furnace));
        WorldPoint[] bank = {
                new WorldPoint(3091, 3500, 0),
                new WorldPoint(3089, 3496, 0),
                new WorldPoint(3091, 3492, 0),
                new WorldPoint(3091, 3487, 0),
                new WorldPoint(3099, 3487, 0),
                new WorldPoint(3099, 3500, 0)
        };
        edgevilleBank = areaService.createPolygonArea(Arrays.asList(bank));
        WorldPoint[] ge = {
                new WorldPoint(3161, 3495, 0),
                new WorldPoint(3157, 3490, 0),
                new WorldPoint(3161, 3483, 0),
                new WorldPoint(3171, 3485, 0),
                new WorldPoint(3172, 3492, 0),
                new WorldPoint(3168, 3496, 0)
        };
        grandExchange = areaService.createPolygonArea(Arrays.asList(ge));

        jewelryScript.start();
        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);
    }

    @Override
    protected void shutDown() {
        jewelryScript.stop();
        overlayManager.remove(scriptOverlay);
        overlayManager.remove(mouseTrackerOverlay);
        overlayManager.remove(sceneOverlay);
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        log.info("Option={}, Target={}, Param0={}, Param1={}, MenuAction={}, ItemId={}, id={}, itemOp={}, str={}",
                event.getMenuOption(), event.getMenuTarget(), event.getParam0(), event.getParam1(), event.getMenuAction().name(), event.getItemId(),
                event.getId(), event.getItemOp(), event);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if(event.getGroup().equals("autojewelry")) {
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
        return jewelryScript.getStatus();
    }
}
