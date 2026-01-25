package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class BankTask extends AbstractTask {
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
        return "Banking";
    }
}
