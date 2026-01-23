package com.krakenplugins.example.jewelry.script.state;

import com.kraken.api.core.script.AbstractTask;

public class WalkToGrandExchange extends AbstractTask {
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
        return "Walking to G.E.";
    }
}
