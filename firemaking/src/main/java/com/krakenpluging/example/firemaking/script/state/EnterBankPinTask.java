package com.krakenpluging.example.firemaking.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.firemaking.FiremakingConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class EnterBankPinTask extends AbstractTask {

    @Inject
    private BankService bankService;

    @Inject
    private FiremakingConfig config;

    @Override
    public boolean validate() {
        return bankService.isPinOpen();
    }

    @Override
    public int execute() {
        String pin = config.bankPin();
        if (pin != null && !pin.isEmpty()) {
            int[] pinNumbers = pin.chars().map(Character::getNumericValue).toArray();
            bankService.enterPin(pinNumbers);
            SleepService.sleepUntil(() -> bankService.isOpen(), 3000);
        } else {
            log.warn("Bank pin interface is open, but no bank pin is configured in settings!");
        }
        return 500;
    }

    @Override
    public String status() {
        return "Entering bank pin";
    }
}
