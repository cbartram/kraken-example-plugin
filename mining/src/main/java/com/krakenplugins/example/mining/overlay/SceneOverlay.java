package com.krakenplugins.example.mining.overlay;

import com.kraken.api.Context;
import com.kraken.api.service.tile.GameArea;
import com.krakenplugins.example.mining.MiningConfig;
import com.krakenplugins.example.mining.MiningPlugin;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

public class SceneOverlay extends Overlay {

    private static final Color INSIDE_FILL = new Color(18, 227, 61, 20);
    private static final Color OUTSIDE_FILL = new Color(223, 41, 41, 20);

    private final Client client;
    private final MiningPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final MiningConfig config;
    private final Context ctx;

    @Inject
    public SceneOverlay(Client client, Context ctx, MiningPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, MiningConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.ctx = ctx;
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

        if(config.debug()) {
            renderDebug(graphics);
        }

        return null;
    }

    private void renderDebug(Graphics2D graphics) {
        renderArea(graphics, plugin.getVarrockBank(), ctx.players().local().isInArea(plugin.getVarrockBank()));
        renderArea(graphics, plugin.getMiningArea(), ctx.players().local().isInArea(plugin.getMiningArea()));
    }

    private void renderArea(Graphics2D graphics, GameArea area, boolean playerInside) {
        if (area == null) {
            return;
        }

        area.render(client, graphics, playerInside ? INSIDE_FILL : OUTSIDE_FILL, false);
        area.render(client, graphics, playerInside ? Color.GREEN : Color.RED, true);
    }

    private void renderTargetRock() {
        GameObject targetRock = plugin.getTargetRock();
        if(targetRock != null) {
            modelOutlineRenderer.drawOutline(targetRock, 2, Color.GREEN, 2);
        }
    }
}
