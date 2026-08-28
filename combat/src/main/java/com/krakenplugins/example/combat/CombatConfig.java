package com.krakenplugins.example.combat;

import net.runelite.client.config.*;

@ConfigGroup("combat")
public interface CombatConfig extends Config {

    @ConfigItem(
            keyName = "licenseKey",
            name = "License Key",
            description = "License key required to enable the plugin.",
            position = 0,
            secret = true
    )
    default String licenseKey() {
        return "";
    }

    @ConfigSection(
            name = "General",
            description = "",
            position = 1
    )
    String general = "General";

    @ConfigItem(
            keyName = "npcTarget",
            name = "NPC Target",
            description = "The name of the NPC to attack.",
            position = 1,
            section = general
    )
    default String npcTarget() {
        return "";
    }

    @ConfigItem(
            keyName = "fightLocation",
            name = "Fight Location",
            description = "World coordinates \"x,y,plane\" of the fight area, e.g. 3161,9891,0 for Varrock sewer moss giants. " +
                    "Leave blank to fight wherever the script is started. Read when the script starts.",
            position = 2,
            section = general
    )
    default String fightLocation() {
        return "";
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            keyName = "eatAt",
            name = "Eat At (% HP)",
            description = "Eats food when health percentage falls to or below this value. A small random amount is added each eat.",
            position = 3,
            section = general
    )
    default int eatAt() {
        return 40;
    }

    @ConfigItem(
            keyName = "buryBones",
            name = "Bury Bones",
            description = "Loots and buries bones from slain NPC's.",
            position = 4,
            section = general
    )
    default boolean buryBones() {
        return false;
    }

    @ConfigSection(
            name = "Banking",
            description = "Options for restocking food at Varrock east bank",
            position = 2
    )
    String banking = "Banking";

    @ConfigItem(
            keyName = "foodName",
            name = "Food",
            description = "The name of the food to withdraw from the bank. i.e. Swordfish",
            position = 1,
            section = banking
    )
    default String foodName() {
        return "Swordfish";
    }

    @Range(min = 1, max = 28)
    @ConfigItem(
            keyName = "foodAmount",
            name = "Food Amount",
            description = "How many food to withdraw. Withdraws less when inventory space is needed for loot or bones.",
            position = 2,
            section = banking
    )
    default int foodAmount() {
        return 10;
    }

    @ConfigSection(
            name = "Loot",
            description = "Options for looting NPC's",
            position = 3
    )
    String loot = "Loot";

    @ConfigItem(
            keyName = "lootIds",
            name = "Loot Ids",
            description = "A comma separated list of item ids which should be looted. i.e. 123,456,89",
            position = 1,
            section = loot
    )
    default String lootIds() {
        return "";
    }

    @Range(min = 1)
    @ConfigItem(
            keyName = "lootValueThreshold",
            name = "Loot Value",
            description = "Ground item stacks worth at least this much are always looted.",
            position = 2,
            section = loot
    )
    default int lootValueThreshold() {
        return 1000;
    }

    @ConfigSection(
            name = "Overlay",
            description = "",
            position = 4
    )
    String overlay = "Overlay";

    @ConfigItem(
            keyName = "highlightTarget",
            name = "Highlight Target",
            description = "Highlights the selected NPC to attack.",
            position = 1,
            section = overlay
    )
    default boolean highlightTarget() {
        return false;
    }

    @ConfigItem(
            keyName = "highlightReachableTiles",
            name = "Highlight Reachable Tiles",
            description = "Highlights the reachable tiles from your current position",
            position = 2,
            section = overlay
    )
    default boolean highlightReachableTiles() {
        return false;
    }

    @Range(min = 1, max = 50)
    @ConfigItem(
            keyName = "reachableTileDist",
            name = "Tile Distance",
            description = "Determines the distance from your player where reachable tiles will be calculated.",
            position = 3,
            section = overlay
    )
    default int reachableTileDist() {
        return 10;
    }
}
