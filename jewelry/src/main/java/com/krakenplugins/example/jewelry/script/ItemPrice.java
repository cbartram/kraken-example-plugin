package com.krakenplugins.example.jewelry.script;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemPrice {
    private int itemId;
    private int high;
    private int low;
    private long highTimestamp;
    private long lowTimestamp;
}
