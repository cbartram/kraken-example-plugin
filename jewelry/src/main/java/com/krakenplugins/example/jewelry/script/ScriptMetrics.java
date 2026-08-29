package com.krakenplugins.example.jewelry.script;


import lombok.Data;

/**
 * Counters shown on the script overlay. Written by the script and by event subscribers, read by the
 * overlay on the client thread, so every field is volatile.
 */
@Data
public class ScriptMetrics {
    private volatile int estimatedProfit = 0;
    private volatile int goldBarsRemaining = 0;
    private volatile int gemsRemaining = 0;
    private volatile int necklacesCrafted = 0;

    /**
     * Clears every counter so a restarted script does not carry the previous run's numbers.
     */
    public void reset() {
        estimatedProfit = 0;
        goldBarsRemaining = 0;
        gemsRemaining = 0;
        necklacesCrafted = 0;
    }
}
