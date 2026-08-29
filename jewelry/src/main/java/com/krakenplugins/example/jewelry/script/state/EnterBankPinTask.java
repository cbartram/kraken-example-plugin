package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class EnterBankPinTask extends AbstractTask {

    private static final int PIN_LENGTH = 4;

    /** Three wrong pins lock the bank, so stop well before that and let the user sort it out. */
    private static final int MAX_ATTEMPTS = 2;

    /** How long the bank has to open after the last digit before the pin counts as rejected. */
    private static final long ACCEPTED_TIMEOUT_MS = 3000;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Inject
    private JewelryPlugin plugin;

    private int attempts;

    @Override
    public boolean validate() {
        return bankService.isPinOpen();
    }

    @Override
    public int execute() {
        String pin = config.bankPin();

        if (pin == null || !pin.matches("\\d{" + PIN_LENGTH + "}")) {
            plugin.halt("Bank pin interface is open, set a " + PIN_LENGTH + " digit pin in the config");
            return 0;
        }

        if (++attempts > MAX_ATTEMPTS) {
            plugin.halt("Bank pin was not accepted, check the configured pin");
            return 0;
        }

        if (!bankService.enterPin(pin.chars().map(Character::getNumericValue).toArray())) {
            log.info("Bank pin was not entered, retrying");
            return 600;
        }

        if (SleepService.sleepUntil(bankService::isOpen, ACCEPTED_TIMEOUT_MS)) {
            attempts = 0;
        }

        return 500;
    }

    @Override
    public String status() {
        return "Entering bank pin";
    }
}
