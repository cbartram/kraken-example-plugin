package com.krakenplugins.example.mining.overlay;

import com.kraken.api.Context;
import com.krakenplugins.example.mining.MiningConfig;
import com.krakenplugins.example.mining.MiningPlugin;
import net.runelite.api.Client;
import com.kraken.api.service.pathfinding.LocalPathfinder;
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
    private final LocalPathfinder pathfinder;
    private final Context ctx;

    @Inject
    public SceneOverlay(Client client, Context ctx, MiningPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, MiningConfig config, LocalPathfinder pathfinder) {
        this.client = client;
        this.plugin = plugin;
        this.ctx = ctx;
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

        if(config.highlightTargetRock()) {
            renderTargetRock();
        }

        if(config.debug()) {
            renderDebug(graphics);
        }

        return null;
    }

    private void renderDebug(Graphics2D graphics) {
        boolean inArea = ctx.players().local().isInArea(plugin.getVarrockBank());
        boolean inAltarArea = ctx.players().local().isInArea(plugin.getMiningArea());

        Color outline = inArea ? Color.GREEN : Color.RED;
        Color fill = inArea ? new Color(18, 227, 61, 20) : new Color(223, 41, 41, 20);

        plugin.getVarrockBank().render(client, graphics, fill, false);
        plugin.getVarrockBank().render(client, graphics, outline, true);


        Color outlineAltar = inAltarArea ? Color.GREEN : Color.RED;
        Color fillAltar = inAltarArea ? new Color(18, 227, 61, 20) : new Color(223, 41, 41, 20);

        plugin.getMiningArea().render(client, graphics, fillAltar, false);
        plugin.getMiningArea().render(client, graphics, outlineAltar, true);
    }

    private void renderTargetRock() {
        if(plugin.getTargetRock() != null) {
            modelOutlineRenderer.drawOutline(plugin.getTargetRock(), 2, Color.GREEN, 2);
        }
    }
}
