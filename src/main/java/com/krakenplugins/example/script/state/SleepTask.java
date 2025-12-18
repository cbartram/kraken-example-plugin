package com.krakenplugins.example.script.state;

import com.krakenplugins.example.script.AbstractTask;

import java.util.Random;

public class SleepTask extends AbstractTask {
    private final Random random = new Random();

    @Override
    public boolean validate() {
        // 1% chance to sleep per tick
        return random.nextInt(100) == 0;
    }

    @Override
    public int execute() {
        // Sleep for 2-5 seconds
        return 2000 + random.nextInt(3000);
    }

    @Override
    public String status() {
        return "Sleeping";
    }
}
