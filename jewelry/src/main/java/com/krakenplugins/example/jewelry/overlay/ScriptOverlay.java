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
        // 1. Setup the Title
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Auto Jewelry")
                .color(ColorScheme.BRAND_ORANGE) // Use standard RuneLite Orange for branding
                .build());

        // 2. Create a Table for perfect alignment (2 columns)
        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        // 3. Add rows to the table
        // Format: Label (Gray/Orange) | Value (White/Green)
        addRow(tableComponent, "Status:", plugin.getStatus(), Color.WHITE);
        addRow(tableComponent, "Runtime:", plugin.getRuntime(), Color.WHITE);

        // Add a spacer row if you want visual separation
        // tableComponent.addRow("", "");

        addRow(tableComponent, "Crafted:", String.valueOf(plugin.getMetrics().getNecklacesCrafted()), ColorScheme.GRAND_EXCHANGE_PRICE);

        // Use your formatting helper here
        String profit = formatProfit(plugin.getMetrics().getEstimatedProfit());
        addRow(tableComponent, "Est Profit:", profit, Color.GREEN);

        addRow(tableComponent, "Gold Bars:", String.valueOf(plugin.getMetrics().getGoldBarsRemaining()), Color.WHITE);
        addRow(tableComponent, "Gems:", String.valueOf(plugin.getMetrics().getGemsRemaining()), Color.WHITE);

        // 4. Add the table to the panel
        panelComponent.getChildren().add(tableComponent);

        return super.render(graphics);
    }

    /**
     * Helper to add a row to the table with consistent coloring
     */
    private void addRow(TableComponent table, String label, String value, Color valueColor) {
        table.addRow(label, value);

        // Set the color of the last added row's elements
        // The table stores elements as RenderableEntity, so we color the last two added (Label, Value)
        int lastRowIndex = table.getRows().size() - 1;
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