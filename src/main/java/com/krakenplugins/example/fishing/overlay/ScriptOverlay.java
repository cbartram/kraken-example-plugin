package com.krakenplugins.example.fishing.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.krakenplugins.example.fishing.FishingPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class ScriptOverlay extends OverlayPanel {

    private static final Color HEADER_COLOR = Color.CYAN;

    private final FishingPlugin plugin;

    @Inject
    private ScriptOverlay(FishingPlugin plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Auto Fisher")
                    .color(HEADER_COLOR)
                    .build());
            panelComponent.getChildren().add(TitleComponent.builder().text("").build());
            addTextLine("Status: " + plugin.getStatus());
            addTextLine("Runtime: " + plugin.getRuntime());
            addTextLine("Fish Caught: " + plugin.getFishCaught());
        return super.render(graphics);
    }

    private void addTextLine(String text) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(text)
                .leftColor(Color.WHITE)
                .build());
    }
}
