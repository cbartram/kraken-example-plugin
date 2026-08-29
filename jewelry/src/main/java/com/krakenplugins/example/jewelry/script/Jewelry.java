package com.krakenplugins.example.jewelry.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;

@Getter
@AllArgsConstructor
public enum Jewelry {
    SAPPHIRE_NECKLACE(InterfaceID.CraftingGold.SAPPHIRE_NECKLACE, "Sapphire necklace", ItemID.SAPPHIRE, ItemID.SAPPHIRE_NECKLACE),
    RUBY_NECKLACE(InterfaceID.CraftingGold.RUBY_NECKLACE, "Ruby necklace", ItemID.RUBY, ItemID.RUBY_NECKLACE);

    // The crafting interface wraps each item name in this colour, and the menu action carries the
    // tags, so the action string has to reproduce them exactly to resolve.
    private static final String NAME_COLOUR = "<col=ff9040>";

    private final int widgetId;
    private final String necklaceName;
    private final int secondaryGemId;
    private final int craftedItemId; // The id of the final product

    /**
     * The menu action the crafting interface puts on this necklace's icon.
     * @return The action string to interact with.
     */
    public String makeAction() {
        return "Make " + NAME_COLOUR + necklaceName + "</col>";
    }
}
