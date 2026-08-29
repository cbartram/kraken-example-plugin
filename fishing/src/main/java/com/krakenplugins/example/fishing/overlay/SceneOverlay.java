package com.krakenplugins.example.fishing.overlay;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.query.npc.NpcEntity;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.FishingPlugin;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.util.List;

@Singleton
public class SceneOverlay extends Overlay {

    private static final int DEBUG_RENDER_DISTANCE = 10;

    private final Client client;
    private final FishingPlugin plugin;
    private final Context ctx;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final FishingConfig config;

    @Inject
    public SceneOverlay(Client client, Context ctx, FishingPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, FishingConfig config) {
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

        if(config.highlightTargetSpot()) {
            renderTargetSpot();
        }

        if(config.highlightDepositBox()) {
            renderDepositBox();
        }

        if(config.debug()) {
            renderNearbySpots(graphics);
        }

        return null;
    }

    private void renderTargetSpot() {
        NpcEntity spot = plugin.getTargetSpot();
        if (spot != null && spot.raw() != null && spot.raw().getModel() != null) {
            modelOutlineRenderer.drawOutline(spot.raw(), 2, Color.GREEN, 2);
        }
    }

    private void renderDepositBox() {
        GameObjectEntity depositBox = plugin.getDepositBox();
        if (depositBox != null
                && depositBox.raw() != null
                && depositBox.raw().getRenderable() != null
                && depositBox.raw().getRenderable().getModel() != null) {
            modelOutlineRenderer.drawOutline(depositBox.raw(), 2, Color.GREEN, 2);
        }
    }

    private void renderNearbySpots(Graphics2D graphics) {
        LocalPoint localPoint = ctx.players().local().localLocation();
        if (localPoint == null) {
            return;
        }

        // This runs every frame, so the spot id does the filtering: reachable() costs a client thread
        // reachability flood per NPC it is handed.
        List<NpcEntity> spots = ctx.npcs()
                .withId(config.fishingLocation().getSpotId())
                .within(DEBUG_RENDER_DISTANCE)
                .reachable()
                .list();

        NpcEntity target = plugin.getTargetSpot();
        for(NpcEntity spot : spots) {
            int distance = localPoint.distanceTo(spot.raw().getLocalLocation()) / Perspective.LOCAL_TILE_SIZE;
            String overlayText = String.format("Dist: %d", distance);
            net.runelite.api.Point textLocation = spot.raw().getCanvasTextLocation(graphics, overlayText, 0);
            if (textLocation == null) {
                continue;
            }

            boolean isTarget = target != null && target.raw() != null
                    && spot.raw().getWorldLocation().equals(target.raw().getWorldLocation());
            Color color = isTarget ? Color.GREEN : Color.CYAN;

            OverlayUtil.renderTextLocation(graphics, textLocation, overlayText, color);

            Shape clickbox = spot.raw().getConvexHull();
            if (clickbox != null) {
                graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
                graphics.fill(clickbox);
                graphics.setColor(color);
                graphics.draw(clickbox);
            }
        }
    }
}
