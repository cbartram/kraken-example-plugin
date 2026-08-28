package com.krakenplugins.example.combat;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.krakenplugins.example.combat.overlay.SceneOverlay;
import com.krakenplugins.example.combat.overlay.ScriptOverlay;
import com.krakenplugins.example.combat.script.CombatScript;
import com.krakenplugins.example.combat.script.ScriptContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Singleton
@PluginDescriptor(
        name = "Combat",
        enabledByDefault = false,
        description = "Fights a configured NPC, eating, looting, and restocking food at Varrock east bank.",
        tags = {"auto", "fighter", "npc", "combat", "kraken"}
)
public class CombatPlugin extends Plugin {

    private static final String SAFE_SPOT = ColorUtil.wrapWithColorTag("Safe Spot", JagexColors.CHAT_PRIVATE_MESSAGE_TEXT_TRANSPARENT_BACKGROUND);

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ScriptOverlay scriptOverlay;

    @Inject
    private SceneOverlay sceneOverlay;

    @Inject
    private CombatScript script;

    @Inject
    private ScriptContext scriptContext;

    private WorldPoint trueTile;

    @Provides
    CombatConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(CombatConfig.class);
    }

    @Override
    protected void startUp() {
        overlayManager.add(scriptOverlay);
        overlayManager.add(sceneOverlay);

        if (client.getGameState() == GameState.LOGGED_IN) {
            script.start();
        }
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(scriptOverlay);
        overlayManager.remove(sceneOverlay);
        script.stop();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        // Covers enabling the plugin while logged out; start() is a no-op when already running
        if (event.getGameState() == GameState.LOGGED_IN) {
            script.start();
        }
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        trueTile = getSelectedWorldPoint();
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.isKeyPressed(KeyCode.KC_SHIFT) && event.getOption().equals("Walk here") && event.getTarget().isEmpty()) {
            addMenuEntry(event, "Set", SAFE_SPOT, 1);
        }
    }

    private void addMenuEntry(MenuEntryAdded event, String option, String target, int position) {
        List<MenuEntry> entries = new LinkedList<>(Arrays.asList(client.getMenu().getMenuEntries()));
        if (entries.stream().anyMatch(e -> e.getOption().equals(option) && e.getTarget().equals(target))) {
            return;
        }

        client.getMenu().createMenuEntry(position)
                .setOption(option)
                .setTarget(target)
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .setIdentifier(event.getIdentifier())
                .setType(MenuAction.RUNELITE)
                .onClick(this::onMenuOptionClicked);
    }

    private void onMenuOptionClicked(MenuEntry entry) {
        if (entry.getOption().equals("Set") && entry.getTarget().equals(SAFE_SPOT)) {
            scriptContext.setSafespot(trueTile);
        }
    }

    private WorldPoint getSelectedWorldPoint() {
        if (client.getWidget(ComponentID.WORLD_MAP_MAPVIEW) == null) {
            if (client.getTopLevelWorldView().getSelectedSceneTile() != null) {
                return client.getTopLevelWorldView().isInstance() ?
                        WorldPoint.fromLocalInstance(client, client.getTopLevelWorldView().getSelectedSceneTile().getLocalLocation()) :
                        client.getTopLevelWorldView().getSelectedSceneTile().getWorldLocation();
            }
        }
        return null;
    }
}
