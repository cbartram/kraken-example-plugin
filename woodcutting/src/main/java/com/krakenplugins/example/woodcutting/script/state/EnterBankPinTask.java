package com.krakenplugins.example.woodcutting.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.woodcutting.WoodcuttingConfig;
import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class EnterBankPinTask extends AbstractTask {

    private static final int PIN_LENGTH = 4;

    /** Three wrong pins lock the bank, so stop well before that and let the user sort it out. */
    private static final int MAX_ATTEMPTS = 2;

    @Inject
    private BankService bankService;

    @Inject
    private WoodcuttingConfig config;

    @Inject
    private WoodcuttingPlugin plugin;

    private int attempts;

    @Override
    public boolean validate() {
        return bankService.isPinOpen();
    }

    @Override
    public int execute() {
        String pin = config.bankPin();

        if (pin == null || !pin.matches("\\d{" + PIN_LENGTH + "}")) {
            plugin.pauseScript("Bank pin interface is open, set a " + PIN_LENGTH + " digit pin in the config");
            return 600;
        }

        if (++attempts > MAX_ATTEMPTS) {
            plugin.pauseScript("Bank pin was not accepted, check the configured pin");
            return 600;
        }

        bankService.enterPin(pin.chars().map(Character::getNumericValue).toArray());

        if (SleepService.sleepUntil(() -> bankService.isOpen(), 3000)) {
            attempts = 0;
        }

        return 500;
    }

    @Override
    public String status() {
        return "Entering bank pin";
    }
}
