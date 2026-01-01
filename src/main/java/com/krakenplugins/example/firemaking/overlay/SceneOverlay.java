package com.krakenplugins.example.firemaking.overlay;

import com.google.inject.Inject;
import com.kraken.api.Context;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;

public class SceneOverlay extends Overlay {

    private final FiremakingPlugin plugin;
    private final FiremakingConfig config;
    private final Context ctx;

    @Inject
    public SceneOverlay(FiremakingPlugin plugin, FiremakingConfig config, Context ctx) {
        this.plugin = plugin;
        this.config = config;
        this.ctx = ctx;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (config.debug()) {
            // Render debug info if needed
        }
        return null;
    }
}
