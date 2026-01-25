package com.krakenplugins.autorunecrafting.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.overlay.table.TableAlignment;
import com.kraken.api.overlay.table.TableComponent;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class ScriptOverlay extends OverlayPanel {

    private final AutoRunecraftingPlugin plugin;

    @Inject
    private ScriptOverlay(AutoRunecraftingPlugin plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);

        setResizable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Auto Runecrafting")
                .color(ColorScheme.BRAND_ORANGE)
                .build());

        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        tableComponent.addRow("Status: ", plugin.getStatus());
        tableComponent.addRow("Runtime: ", plugin.getRuntime());

        panelComponent.getChildren().add(tableComponent);

        return super.render(graphics);
    }

    private String formatProfit(long profit) {
        if (profit < 1000) {
            return String.valueOf(profit);
        } else if (profit < 1000000) {
            return (profit / 1000) + "k";
        } else {
            return (profit / 1000000) + "m";
        }
    }
}