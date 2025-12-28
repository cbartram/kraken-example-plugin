package com.krakenplugins.example.fishing.script.state;

import com.kraken.api.core.script.PriorityTask;

public class FishKaramja extends PriorityTask {
    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public int execute() {
        return 0;
    }

    @Override
    public String status() {
        return "";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
