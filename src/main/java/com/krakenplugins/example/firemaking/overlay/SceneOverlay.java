package com.krakenplugins.example.firemaking.overlay;

import com.google.inject.Inject;
import com.kraken.api.Context;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import com.krakenplugins.example.firemaking.script.state.FindPathTask;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;

public class SceneOverlay extends Overlay {

    private final FiremakingPlugin plugin;
    private final FiremakingConfig config;
    private final Context ctx;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final Client client;
    private final FindPathTask findPathTask;

    private static final Color COLOR_START_SPOT = new Color(0, 255, 255, 242); // Cyan
    private static final Color COLOR_LINE = new Color(255, 165, 0, 180);       // Orange

    // Caching fields to prevent FPS drops
    private WorldPoint cachedBestSpot = null;
    private long lastCheckTime = 0;

    @Inject
    public SceneOverlay(FiremakingPlugin plugin, FindPathTask findPathTask, Client client, FiremakingConfig config, Context ctx, ModelOutlineRenderer modelOutlineRenderer) {
        this.plugin = plugin;
        this.config = config;
        this.ctx = ctx;
        this.client = client;
        this.findPathTask = findPathTask;
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

        if (config.showFireLine()) {
            renderFireLine(graphics);
        }

        return null;
    }

    private void renderFireLine(Graphics2D graphics) {
        // Use a cached lookup to avoid heavy scanning every frame
        WorldPoint bestSpot = getBestSpotCached();

        if (bestSpot == null) {
            return;
        }

        long logsCount = ctx.inventory().withName(config.logName()).count();
        if (logsCount == 0) return;

        WorldPoint current = bestSpot;

        // Direction is always West for your script
        int dx = -1;
        int dy = 0;

        for (int i = 0; i < logsCount; i++) {
            // Logic must match FindPathTask: Stop if we hit a Fire or a Wall
            if (isTileBlockedOrHasFire(current)) {
                break;
            }

            // Draw the tile
            Color c = (i == 0) ? COLOR_START_SPOT : COLOR_LINE;
            renderTile(graphics, current, c);

            // Move to next tile for the next iteration
            current = current.dx(dx).dy(dy);
        }
    }

    /**
     * Replicates the validation logic from FindPathTask to ensure
     * the overlay shows exactly what the bot sees.
     */
    private boolean isTileBlockedOrHasFire(WorldPoint p) {
        // 1. Check for Fire object
        if (ctx.gameObjects().at(p).withName("Fire").first() != null) return true;

        // 2. Check collision/walkability (using faster check than isReachable)
        return !ctx.getTileService().isTileReachable(p);
    }

    /**
     * Prevents calling the heavy findBestSpot() method 50 times a second.
     * Updates the spot at most once every 600ms (one game tick).
     */
    private WorldPoint getBestSpotCached() {
        if (System.currentTimeMillis() - lastCheckTime > 600) {
            cachedBestSpot = findPathTask.findBestSpot();
            lastCheckTime = System.currentTimeMillis();
        }
        return cachedBestSpot;
    }

    private void renderTile(Graphics2D graphics, WorldPoint wp, Color color) {
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp != null) {
            Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            if (poly != null) {
                OverlayUtil.renderPolygon(graphics, poly, color);
            }
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
    }
}