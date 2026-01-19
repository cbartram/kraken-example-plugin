package com.krakenplugins.example.jewelry.script;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Jewelry {
    SAPPHIRE_NECKLACE(29229080, "sapphire necklace", 1607),
    RUBY_NECKLACE(29229082, "ruby necklace", 1603);

    private final int widgetId;
    private final String necklaceName;
    private final int secondaryGemId;
}
