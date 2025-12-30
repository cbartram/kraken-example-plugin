package com.krakenplugins.example.jewelry.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;

import java.awt.*;

import static com.krakenplugins.example.jewelry.script.JewelryScript.EDGEVILLE_BANK;
import static com.krakenplugins.example.jewelry.script.JewelryScript.EDGEVILLE_FURNACE;

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
        LocalPoint localPoint = LocalPoint.fromWorld(client, EDGEVILLE_BANK);

        if (localPoint != null) {
            // Check if player is in area using your requested method
            boolean inArea = ctx.players().local().isInArea(EDGEVILLE_BANK, 5);

            // Set Color based on state
            Color color = inArea ? Color.GREEN : Color.RED;

            // Render the area
            // Note: Perspective.getCanvasTileAreaPoly takes 'size' as diameter.
            // A radius of 3 implies 3 tiles in every direction + the center tile = 7 total width.
            Polygon areaPoly = Perspective.getCanvasTileAreaPoly(client, localPoint, (5 * 2) + 1);

            if (areaPoly != null) {
                OverlayUtil.renderPolygon(graphics, areaPoly, color);
            }
        }


        LocalPoint furnace = LocalPoint.fromWorld(client, EDGEVILLE_FURNACE);
        if (furnace != null) {
            // Check if player is in area using your requested method
            boolean inArea = ctx.players().local().isInArea(EDGEVILLE_FURNACE, 3);

            // Set Color based on state
            Color color = inArea ? Color.GREEN : Color.RED;

            // Render the area
            // Note: Perspective.getCanvasTileAreaPoly takes 'size' as diameter.
            // A radius of 3 implies 3 tiles in every direction + the center tile = 7 total width.
            Polygon areaPoly = Perspective.getCanvasTileAreaPoly(client, furnace, (3 * 2) + 1);

            if (areaPoly != null) {
                OverlayUtil.renderPolygon(graphics, areaPoly, color);
            }
        }
    }
}
