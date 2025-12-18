package com.krakenplugins.example.overlay;

import com.krakenplugins.example.MiningConfig;
import com.krakenplugins.example.MiningPlugin;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

public class SceneOverlay extends Overlay {
    private final Client client;
    private final MiningPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final MiningConfig config;

    @Inject
    public SceneOverlay(Client client, MiningPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, MiningConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.config = config;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.client.getCanvas() == null) {
            return null;
        }

        if(config.highlightTargetRock()) {
            renderTargetRock();
        }

        return null;
    }

    private void renderTargetRock() {
        if(plugin.getTargetRock() != null) {
            modelOutlineRenderer.drawOutline(plugin.getTargetRock(), 2, Color.GREEN, 2);
        }
    }
}
