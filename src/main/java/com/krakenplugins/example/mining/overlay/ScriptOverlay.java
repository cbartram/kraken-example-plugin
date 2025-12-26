package com.krakenplugins.example.mining.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.krakenplugins.example.mining.MiningPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class ScriptOverlay extends OverlayPanel {

    private static final Color HEADER_COLOR = ColorScheme.BRAND_ORANGE;

    private final MiningPlugin miningPlugin;

    @Inject
    private ScriptOverlay(MiningPlugin miningPlugin) {
        this.miningPlugin = miningPlugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Auto Miner")
                    .color(HEADER_COLOR)
                    .build());
            panelComponent.getChildren().add(TitleComponent.builder().text("").build());
            addTextLine("Status: " + miningPlugin.getStatus());
            addTextLine("Runtime: " + miningPlugin.getRuntime());
            addTextLine("Ore Mined: " + miningPlugin.getOreMined());
        return super.render(graphics);
    }

    private void addTextLine(String text) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(text)
                .leftColor(Color.WHITE)
                .build());
    }
}
