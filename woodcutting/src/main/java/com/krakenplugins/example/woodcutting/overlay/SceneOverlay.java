package com.krakenplugins.example.woodcutting.overlay;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.util.List;

import static com.krakenplugins.example.woodcutting.WoodcuttingPlugin.BANK_LOCATION;

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

    private void renderDebug(Graphics2D graphics) {
        LocalPoint localPoint = LocalPoint.fromWorld(client, BANK_LOCATION);

        if (localPoint != null) {
            // Check if player is in area using your requested method
            boolean inArea = ctx.players().local().isInArea(BANK_LOCATION, 3);

            // Set Color based on state
            Color color = inArea ? Color.GREEN : Color.RED;

            // Render the area
            // Note: Perspective.getCanvasTileAreaPoly takes 'size' as diameter.
            // A radius of 3 implies 3 tiles in every direction + the center tile = 7 total width.
            Polygon areaPoly = Perspective.getCanvasTileAreaPoly(client, localPoint, (3 * 2) + 1);

            if (areaPoly != null) {
                OverlayUtil.renderPolygon(graphics, areaPoly, color);
            }
        }
    }

    private void renderTargetTree() {
        if(plugin.getTargetTree() != null) {
            modelOutlineRenderer.drawOutline(plugin.getTargetTree(), 2, Color.GREEN, 2);
        }
    }

    private void renderTreeRadius(Graphics2D graphics) {
        if (client.getLocalPlayer() == null) {
            return;
        }
        LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
        if (localPoint != null) {
            List<GameObjectEntity> trees = ctx.gameObjects()
                    .within(config.treeRadius())
                    .reachable()
                    .withName(config.treeName())
                    .list();

            for(GameObjectEntity tree : trees) {
                int distance = localPoint.distanceTo(tree.raw().getLocalLocation()) / Perspective.LOCAL_TILE_SIZE;

                String overlayText = String.format("Dist: %d", distance);
                net.runelite.api.Point textLocation = tree.raw().getCanvasTextLocation(graphics, overlayText, 0);

                if (textLocation != null) {
                    Color color;

                    if(plugin.getTargetTree() != null) {
                        if (tree.raw().getWorldLocation().getX() == plugin.getTargetTree().getWorldLocation().getX() &&
                                tree.raw().getWorldLocation().getY() == plugin.getTargetTree().getWorldLocation().getY()) {
                            color = Color.GREEN;
                        } else {
                            color = Color.CYAN;
                        }
                    } else {
                        color = Color.CYAN;
                    }

                    OverlayUtil.renderTextLocation(graphics, textLocation, overlayText, color);

                    if (tree.raw().getClickbox() != null) {
                        OverlayUtil.renderPolygon(graphics, tree.raw().getClickbox(), color);
                    }
                }
            }
        }
    }
}