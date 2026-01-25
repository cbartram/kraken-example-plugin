package com.krakenplugins.autorunecrafting.overlay;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.kraken.api.service.pathfinding.LocalPathfinder;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;

@Singleton
public class SceneOverlay extends Overlay {
    private final Client client;
    private final AutoRunecraftingPlugin plugin;
    private final Context ctx;
    private final AutoRunecraftingConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final LocalPathfinder localPathfinder;

    @Inject
    public SceneOverlay(Client client, Context ctx, AutoRunecraftingPlugin plugin, AutoRunecraftingConfig config, ModelOutlineRenderer modelOutlineRenderer, LocalPathfinder localPathfinder) {
        this.client = client;
        this.plugin = plugin;
        this.ctx = ctx;
        this.config = config;
        this.localPathfinder = localPathfinder;
        this.modelOutlineRenderer = modelOutlineRenderer;

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

        if(config.showCurrentPath()) {
            localPathfinder.renderPath(plugin.getCurrentPath(), graphics, new Color(24, 191, 243));
        }

        if(config.targetBankBooth()) {
            renderTargetBankBooth();
        }

        return null;
    }

    private void renderTargetBankBooth() {
        GameObjectEntity booth = plugin.getTargetBankBooth();
        if(booth != null && booth.raw() != null) {
            modelOutlineRenderer.drawOutline(booth.raw(), 2, Color.GREEN, 2);
        }
    }

    private void renderDebug(Graphics2D graphics) {
        boolean inArea = ctx.players().local().isInArea(plugin.getFaladorBank());

        Color outline = inArea ? Color.GREEN : Color.RED;
        Color fill = inArea ? new Color(18, 227, 61, 20) : new Color(223, 41, 41, 20);

        plugin.getFaladorBank().render(client, graphics, fill, false);
        plugin.getFaladorBank().render(client, graphics, outline, true);
    }
}
