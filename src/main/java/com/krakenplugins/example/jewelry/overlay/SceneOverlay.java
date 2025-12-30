package com.krakenplugins.example.jewelry.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.*;

@Singleton
public class SceneOverlay extends Overlay {
    private final Client client;
    private final JewelryPlugin plugin;
    private final Context ctx;
    private final JewelryConfig config;

    @Inject
    public SceneOverlay(Client client, Context ctx, JewelryPlugin plugin, JewelryConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.ctx = ctx;
        this.config = config;

        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.client.getCanvas() == null) {
            return null;
        }

        if(config.debug()) {
            renderDebug(graphics);
        }

        return null;
    }

    private void renderDebug(Graphics2D graphics) {
        // Implement debug rendering
    }
}
