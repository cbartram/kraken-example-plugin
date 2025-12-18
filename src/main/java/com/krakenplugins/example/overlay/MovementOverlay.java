package com.krakenplugins.example.overlay;

import com.kraken.api.service.movement.Pathfinder;
import com.krakenplugins.example.MiningPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class MovementOverlay extends Overlay {
    private final Pathfinder pathfinder;
    private final MiningPlugin plugin;

    @Inject
    public MovementOverlay(MiningPlugin plugin, Pathfinder pathfinder) {
        this.plugin = plugin;
        this.pathfinder = pathfinder;

        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics2D) {
        // TODO Get the path from the plugin
        return pathfinder.renderPath(plugin.getCurrentPath(), graphics2D);
    }
}
