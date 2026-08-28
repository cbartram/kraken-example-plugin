package com.krakenplugins.example.combat.overlay;

import com.krakenplugins.example.combat.CombatConfig;
import com.krakenplugins.example.combat.script.ScriptContext;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

public class SceneOverlay extends Overlay {
    private final Client client;
    private final ScriptContext scriptContext;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final CombatConfig config;

    @Inject
    public SceneOverlay(Client client, ScriptContext scriptContext, ModelOutlineRenderer modelOutlineRenderer, CombatConfig config) {
        this.client = client;
        this.scriptContext = scriptContext;
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

        if (config.highlightTarget()) {
            renderTarget();
        }

        if (config.highlightReachableTiles()) {
            renderReachableTiles(graphics);
        }

        renderSafespot(graphics);

        return null;
    }

    private void renderSafespot(Graphics2D g) {
        if (scriptContext.getSafespot() != null) {
            LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), scriptContext.getSafespot());
            if (lp == null) return;
            Polygon polygon = Perspective.getCanvasTilePoly(client, lp);
            if (polygon == null) return;

            renderPolygon(g, polygon, new Color(241, 160, 9), new Color(241, 160, 9, 20), new BasicStroke(2));
        }
    }

    private void renderReachableTiles(Graphics2D g) {
        for (WorldPoint tile : scriptContext.getReachableTiles()) {
            LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), tile);
            if (lp == null) continue;
            Polygon polygon = Perspective.getCanvasTilePoly(client, lp);
            if (polygon == null) continue;

            renderPolygon(g, polygon, new Color(9, 241, 107), new Color(9, 241, 107, 20), new BasicStroke(2));
        }
    }

    private void renderTarget() {
        if (scriptContext.getTarget() != null) {
            modelOutlineRenderer.drawOutline(scriptContext.getTarget(), 2, Color.GREEN, 2);
        }
    }

    public static void renderPolygon(Graphics2D graphics, Shape poly, Color color, Color fillColor, Stroke borderStroke) {
        graphics.setColor(color);
        final Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(borderStroke);
        graphics.draw(poly);
        graphics.setColor(fillColor);
        graphics.fill(poly);
        graphics.setStroke(originalStroke);
    }
}
