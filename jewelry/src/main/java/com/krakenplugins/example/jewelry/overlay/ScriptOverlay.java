package com.krakenplugins.example.jewelry.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class ScriptOverlay extends OverlayPanel {

    private static final Color HEADER_COLOR = Color.GREEN;

    private final JewelryPlugin plugin;

    @Inject
    private ScriptOverlay(JewelryPlugin plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Auto Jewelry")
                    .color(HEADER_COLOR)
                    .build());
            panelComponent.getChildren().add(TitleComponent.builder().text("").build());
            addTextLine("Status: " + plugin.getStatus());
            addTextLine("Runtime: " + plugin.getRuntime());
        return super.render(graphics);
    }

    private void addTextLine(String text) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(text)
                .leftColor(Color.WHITE)
                .build());
    }
}
