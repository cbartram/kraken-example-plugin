package com.krakenplugins.example.firemaking.overlay;

import com.google.inject.Inject;
import com.kraken.api.Context;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;

public class SceneOverlay extends Overlay {

    /** Forester's Campfire. Has no gameval constant, the cache symbol is {@code forestry_fire}. */
    private static final int FORESTERS_CAMPFIRE = 49927;

    private static final int RENDER_DISTANCE = 10;

    private final FiremakingPlugin plugin;
    private final FiremakingConfig config;
    private final Context ctx;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final Client client;

    @Inject
    public SceneOverlay(FiremakingPlugin plugin, Client client, FiremakingConfig config, Context ctx, ModelOutlineRenderer modelOutlineRenderer) {
        this.plugin = plugin;
        this.config = config;
        this.ctx = ctx;
        this.client = client;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (config.debug()) {
            renderDebug(graphics);
        }

        if (config.showBanker()) {
            renderTargetBanker();
        }

        if (config.showTargetFire()) {
            renderTargetFire();
        }

        return null;
    }

    private void renderTargetFire() {
        GameObject fire = plugin.getTargetFire();
        if (fire != null) {
            modelOutlineRenderer.drawOutline(fire, 2, Color.GREEN, 2);
        }
    }

    private void renderTargetBanker() {
        NPC banker = plugin.getTargetBanker();
        if (banker != null) {
            modelOutlineRenderer.drawOutline(banker, 2, Color.GREEN, 2);
        }
    }

    private void renderDebug(Graphics2D graphics) {
        boolean inArea = ctx.players().local().isInArea(plugin.getBankLocation());

        Color outline = inArea ? Color.GREEN : Color.RED;
        Color fill = inArea ? new Color(18, 227, 61, 20) : new Color(223, 41, 41, 20);

        plugin.getBankLocation().render(client, graphics, fill, false);
        plugin.getBankLocation().render(client, graphics, outline, true);

        LocalPoint localPoint = ctx.players().local().localLocation();
        if (localPoint == null) {
            return;
        }

        // Both fire types come out of a single scene pass, since this runs every frame.
        ctx.gameObjects()
                .filter(o -> o.getId() == ObjectID.FIRE || o.getId() == FORESTERS_CAMPFIRE)
                .within(RENDER_DISTANCE)
                .stream()
                .forEach(entity -> {
                    GameObject fire = entity.raw();
                    if (fire == plugin.getTargetFire()) {
                        return;
                    }

                    renderDistance(graphics, fire, localPoint,
                            fire.getId() == FORESTERS_CAMPFIRE ? Color.MAGENTA : Color.CYAN);
                });
    }

    private void renderDistance(Graphics2D graphics, GameObject fire, LocalPoint from, Color color) {
        int distance = from.distanceTo(fire.getLocalLocation()) / Perspective.LOCAL_TILE_SIZE;
        String overlayText = String.format("Dist: %d", distance);

        net.runelite.api.Point textLocation = fire.getCanvasTextLocation(graphics, overlayText, 0);
        if (textLocation == null) {
            return;
        }

        OverlayUtil.renderTextLocation(graphics, textLocation, overlayText, color);

        if (fire.getClickbox() != null) {
            OverlayUtil.renderPolygon(graphics, fire.getClickbox(), color);
        }
    }
}
