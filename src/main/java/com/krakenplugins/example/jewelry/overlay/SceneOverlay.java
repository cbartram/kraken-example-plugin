package com.krakenplugins.example.jewelry.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.query.gameobject.GameObjectEntity;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import com.krakenplugins.example.jewelry.script.JewelryScript;
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
    private final JewelryPlugin plugin;
    private final JewelryScript script;
    private final Context ctx;
    private final JewelryConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    public SceneOverlay(Client client, Context ctx, JewelryPlugin plugin, JewelryConfig config, ModelOutlineRenderer modelOutlineRenderer, JewelryScript script) {
        this.client = client;
        this.plugin = plugin;
        this.ctx = ctx;
        this.config = config;
        this.script = script;
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
        boolean inArea = ctx.players().local().isInArea(script.getEdgevilleBank());
        boolean inFurnace = ctx.players().local().isInArea(script.getEdgevilleFurnace());

        Color color = inArea ? Color.GREEN : Color.RED;
        Color fill = inArea ? new Color(18, 227, 61, 45) : new Color(223, 41, 41, 65);

        script.getEdgevilleBank().render(client, graphics, fill, false);
        script.getEdgevilleBank().render(client, graphics, color, true);

        Color furnaceOutline = inFurnace ? Color.GREEN : Color.RED;
        Color furnaceFill = inArea ? new Color(18, 227, 61, 45) : new Color(223, 41, 41, 65);

        script.getEdgevilleFurnace().render(client, graphics, furnaceOutline, false);
        script.getEdgevilleFurnace().render(client, graphics, furnaceFill, true);
    }
}
