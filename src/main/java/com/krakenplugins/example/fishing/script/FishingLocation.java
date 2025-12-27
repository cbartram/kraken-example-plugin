package com.krakenplugins.example.fishing.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

@Getter
@AllArgsConstructor
public enum FishingLocation {
    DRAYNOR_VILLAGE(new WorldPoint(3088, 3228, 0), 1525, List.of(317, 321)),
    KARAMJA(new WorldPoint(2924, 3179, 0), -1, List.of()),
    BARBARIAN_VILLAGE(new WorldPoint(3104, 3430, 0), -1, List.of());

    private final WorldPoint location;
    private final int spotId;
    private final List<Integer> fishIds;
}
