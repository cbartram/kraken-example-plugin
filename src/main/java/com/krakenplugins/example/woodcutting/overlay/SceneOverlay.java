package com.krakenplugins.example.woodcutting.overlay;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;

@Singleton
public class SceneOverlay extends Overlay {
    private final Client client;
    private final WoodcuttingPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final WoodcuttingConfig config;
    private final LocalPathfinder pathfinder;

    @Inject
    public SceneOverlay(Client client, WoodcuttingPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, WoodcuttingConfig config, LocalPathfinder pathfinder) {
        this.client = client;
        this.plugin = plugin;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.config = config;
        this.pathfinder = pathfinder;

        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.client.getCanvas() == null) {
            return null;
        }

        if(config.renderPath()) {
            pathfinder.renderPath(plugin.getCurrentPath(), graphics, Color.GREEN);
        }

        if(config.highlightTargetTree()) {
            renderTargetRock();
        }

        return null;
    }

    private void renderTargetRock() {
        if(plugin.getTargetTree() != null) {
            modelOutlineRenderer.drawOutline(plugin.getTargetTree(), 2, Color.GREEN, 2);
        }
    }
}