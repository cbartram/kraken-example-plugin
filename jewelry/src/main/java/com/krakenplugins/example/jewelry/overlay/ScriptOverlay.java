package com.krakenplugins.example.jewelry.overlay;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.overlay.table.TableAlignment;
import com.kraken.api.overlay.table.TableComponent;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class ScriptOverlay extends OverlayPanel {

    private final JewelryPlugin plugin;

    @Inject
    private ScriptOverlay(JewelryPlugin plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);

        setResizable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Auto Jewelry")
                .color(ColorScheme.BRAND_ORANGE)
                .build());

        TableComponent table = new TableComponent();
        table.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        table.addRow("Status:", plugin.getStatus());
        table.addRow("Runtime:", plugin.getRuntime());
        table.addRow("Crafted:", String.valueOf(plugin.getMetrics().getNecklacesCrafted()));
        table.addRow("Est Profit:", formatProfit(plugin.getMetrics().getEstimatedProfit()));
        table.addRow("Gold Bars:", String.valueOf(plugin.getMetrics().getGoldBarsRemaining()));
        table.addRow("Gems:", String.valueOf(plugin.getMetrics().getGemsRemaining()));

        panelComponent.getChildren().add(table);

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
