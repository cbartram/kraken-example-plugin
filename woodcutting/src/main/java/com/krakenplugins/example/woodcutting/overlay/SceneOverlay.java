package com.krakenplugins.example.woodcutting.overlay;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.util.List;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.TREE_AREA_RADIUS;
import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.TREE_LOCATION;

@Singleton
public class SceneOverlay extends Overlay {
    private final Client client;
    private final WoodcuttingPlugin plugin;
    private final Context ctx;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final WoodcuttingConfig config;
    private final LocalPathfinder pathfinder;

    @Inject
    public SceneOverlay(Client client, Context ctx, WoodcuttingPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, WoodcuttingConfig config, LocalPathfinder pathfinder) {
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

        if(config.highlightTargetTree()) {
            renderTargetTree();
        }

        if(config.showTreeRadius()) {
            renderTreeRadius(graphics);
        }

        if(config.debug()) {
            renderDebug(graphics);
        }

        return null;
    }

    /**
     * Draws the area the script treats as "at the trees", which is what decides between walking and
     * chopping, in green while the player is inside it.
     */
    private void renderDebug(Graphics2D graphics) {
        LocalPoint localPoint = LocalPoint.fromWorld(client, TREE_LOCATION);

        if (localPoint != null) {
            boolean inArea = ctx.players().local().isInArea(TREE_LOCATION, TREE_AREA_RADIUS);
            Color color = inArea ? Color.GREEN : Color.RED;

            // getCanvasTileAreaPoly takes a diameter, so a radius covers that many tiles either side
            // of the centre tile.
            Polygon areaPoly = Perspective.getCanvasTileAreaPoly(client, localPoint, (TREE_AREA_RADIUS * 2) + 1);

            if (areaPoly != null) {
                OverlayUtil.renderPolygon(graphics, areaPoly, color);
            }
        }
    }

    private void renderTargetTree() {
        GameObject targetTree = plugin.getTargetTree();
        if(targetTree != null) {
            modelOutlineRenderer.drawOutline(targetTree, 2, Color.GREEN, 2);
        }
    }

    private void renderTreeRadius(Graphics2D graphics) {
        LocalPoint localPoint = ctx.players().local().localLocation();
        if (localPoint == null) {
            return;
        }

        // One scene pass, cheapest filters first, since this runs every frame.
        List<GameObjectEntity> trees = ctx.gameObjects()
                .within(config.treeRadius())
                .withName(config.treeName())
                .list();

        GameObject targetTree = plugin.getTargetTree();

        for(GameObjectEntity tree : trees) {
            int distance = localPoint.distanceTo(tree.raw().getLocalLocation()) / Perspective.LOCAL_TILE_SIZE;

            String overlayText = String.format("Dist: %d", distance);
            net.runelite.api.Point textLocation = tree.raw().getCanvasTextLocation(graphics, overlayText, 0);

            if (textLocation != null) {
                Color color = tree.raw() == targetTree ? Color.GREEN : Color.CYAN;

                OverlayUtil.renderTextLocation(graphics, textLocation, overlayText, color);

                if (tree.raw().getClickbox() != null) {
                    OverlayUtil.renderPolygon(graphics, tree.raw().getClickbox(), color);
                }
            }
        }
    }
}
