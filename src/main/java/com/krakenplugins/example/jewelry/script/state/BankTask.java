package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public int execute() {
        return 1200;
    }

    @Override
    public String status() {
        return "Banking";
    }
}
