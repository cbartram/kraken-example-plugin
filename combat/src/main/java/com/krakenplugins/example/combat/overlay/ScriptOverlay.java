package com.krakenplugins.example.combat.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.krakenplugins.example.combat.script.ScriptContext;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class ScriptOverlay extends OverlayPanel {

    private static final Color HEADER_COLOR = ColorScheme.BRAND_ORANGE;

    private final ScriptContext scriptContext;

    @Inject
    private ScriptOverlay(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Combat")
                .color(HEADER_COLOR)
                .build());
        panelComponent.getChildren().add(TitleComponent.builder().text("").build());
        addTextLine("Status: " + scriptContext.getStatus());
        addTextLine("Runtime: " + runtime());
        return super.render(graphics);
    }

    private String runtime() {
        if (scriptContext.getStartTimeMillis() == 0) {
            return "00:00:00";
        }
        long millis = System.currentTimeMillis() - scriptContext.getStartTimeMillis();
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    private void addTextLine(String text) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(text)
                .leftColor(Color.WHITE)
                .build());
    }
}
