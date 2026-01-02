package com.krakenplugins.example.firemaking.overlay;

import com.google.inject.Inject;
import com.kraken.api.Context;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;

public class SceneOverlay extends Overlay {

    private final FiremakingPlugin plugin;
    private final FiremakingConfig config;
    private final Context ctx;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    public SceneOverlay(FiremakingPlugin plugin, FiremakingConfig config, Context ctx, ModelOutlineRenderer modelOutlineRenderer) {
        this.plugin = plugin;
        this.config = config;
        this.ctx = ctx;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }


    public Dimension render(Graphics2D graphics) {
        if(config.debug()) {
            renderDebug(graphics);
        }

        if(config.showBanker()) {
            renderTargetBanker();
        }

        return null;
    }

    private void renderTargetBanker() {
        GameObjectEntity booth = plugin.getTargetBankBooth();
        if(booth != null && booth.raw() != null) {
            modelOutlineRenderer.drawOutline(booth.raw(), 2, Color.GREEN, 2);
        }
    }

    private void renderDebug(Graphics2D graphics) {
        boolean inArea = ctx.players().local().isInArea(plugin.getEdgevilleBank());
        boolean inFurnace = ctx.players().local().isInArea(plugin.getEdgevilleFurnace());

        Color outline = inArea ? Color.GREEN : Color.RED;
        Color fill = inArea ? new Color(18, 227, 61, 20) : new Color(223, 41, 41, 20);

        plugin.getEdgevilleBank().render(client, graphics, fill, false);
        plugin.getEdgevilleBank().render(client, graphics, outline, true);

        Color furnaceOutline = inFurnace ? Color.GREEN : Color.RED;
        Color furnaceFill = inFurnace ? new Color(18, 227, 61, 20) : new Color(223, 41, 41, 20);

        plugin.getEdgevilleFurnace().render(client, graphics, furnaceFill, false);
        plugin.getEdgevilleFurnace().render(client, graphics, furnaceOutline, true);
    }
}
