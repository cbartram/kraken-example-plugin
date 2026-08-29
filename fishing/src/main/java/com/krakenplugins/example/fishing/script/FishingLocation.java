package com.krakenplugins.example.fishing.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;

import java.util.List;

/**
 * The places this plugin can fish, and what to expect there.
 *
 * <p>Fishing spot NPCs are named in the cache after the fish table they roll on rather than after the
 * place they sit in, which is why the {@code NpcID} constants read the way they do.</p>
 */
@Getter
@AllArgsConstructor
public enum FishingLocation {
    // Small Net / Bait spot: shrimps and anchovies.
    DRAYNOR_VILLAGE(new WorldPoint(3088, 3228, 0), NpcID._0_48_50_SALTFISH,
            List.of(ItemID.RAW_SHRIMP, ItemID.RAW_ANCHOVIES, ItemID.BURNT_SHRIMP, ItemID.BURNTFISH1)),

    // Cage / Harpoon spot: lobster, tuna and swordfish.
    KARAMJA(new WorldPoint(2924, 3179, 0), NpcID._0_45_49_RAREFISH,
            List.of(ItemID.RAW_TUNA, ItemID.RAW_SWORDFISH, ItemID.RAW_LOBSTER)),

    // Cage / Harpoon spot in the Corsair Cove Resource Area.
    CORSAIR_COVE(new WorldPoint(2456, 2892, 0), NpcID._0_38_45_RAREFISH,
            List.of(ItemID.RAW_TUNA, ItemID.RAW_SWORDFISH, ItemID.RAW_LOBSTER)),

    // Rod Fishing spot, Lure / Bait: trout and salmon.
    BARBARIAN_VILLAGE(new WorldPoint(3104, 3430, 0), NpcID._0_48_53_FRESHFISH,
            List.of(ItemID.RAW_TROUT, ItemID.TROUT, ItemID.RAW_SALMON, ItemID.SALMON, ItemID.BURNTFISH2));

    /** Centre of the fishing area. Every area check in the tasks is relative to this point. */
    private final WorldPoint location;

    /** The fishing spot NPC to interact with. */
    private final int spotId;

    /**
     * Everything fishing here can leave in the inventory, so dropping and depositing also clear the
     * cooked and burnt results rather than stopping on them.
     */
    private final List<Integer> fishIds;
}
