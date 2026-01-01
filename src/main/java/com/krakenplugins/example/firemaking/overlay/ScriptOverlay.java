package com.krakenplugins.example.firemaking.overlay;

import com.google.inject.Inject;
import com.krakenplugins.example.firemaking.FiremakingPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class ScriptOverlay extends OverlayPanel {

    private final FiremakingPlugin plugin;

    @Inject
    public ScriptOverlay(FiremakingPlugin plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Firemaking Script")
                .color(Color.ORANGE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(plugin.getStatus())
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Runtime:")
                .right(plugin.getRuntime())
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Logs Burned:")
                .right(Integer.toString(plugin.getLogsBurned()))
                .build());

        return super.render(graphics);
    }
}
