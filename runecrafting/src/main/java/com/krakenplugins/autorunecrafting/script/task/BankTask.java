package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import lombok.extern.slf4j.Slf4j;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.*;

@Slf4j
@Singleton
public class BankTask extends AbstractTask {

    /** How long to wait for the server to reflect a deposit, a withdrawal or an equip. */
    private static final int ACTION_TIMEOUT_MS = 3_000;

    @Inject
    private AutoRunecraftingPlugin plugin;

    @Inject
    private BankService bankService;

    @Override
    public boolean validate() {
        // An open bank is proof enough that we are at one, so there is no area check here. Requiring
        // one would deadlock the script whenever the bank is opened from a tile outside the polygon.
        return bankService.isOpen() && !hasEssence(ctx);
    }

    @Override
    public int execute() {
        if (!ctx.inventory().isEmpty()) {
            bankService.depositAll();
            SleepService.sleepUntil(() -> ctx.inventory().isEmpty(), ACTION_TIMEOUT_MS);
        }

        if (!wearAirTiara()) {
            return 600;
        }

        BankEntity essence = ctx.bank().withId(PURE_ESSENCE).first().orElse(null);
        if (essence == null) {
            log.info("Pure essence not found in bank. Using rune essence");
            essence = ctx.bank().withId(RUNE_ESSENCE).first().orElse(null);
        }

        if (essence == null) {
            plugin.pauseScript("No pure or rune essence left in the bank");
            bankService.close();
            return 600;
        }

        essence.withdrawAll();
        SleepService.sleepUntil(() -> hasEssence(ctx), ACTION_TIMEOUT_MS);

        // The bank is left open, WalkToAltarTask closes it on its way out.
        return RandomService.between(750, 1120);
    }

    /**
     * Puts the air tiara on the player's head, taking it out of the bank when it is not already worn.
     *
     * @return True once the tiara is equipped, false when it could not be.
     */
    private boolean wearAirTiara() {
        if (ctx.equipment().isWearing(AIR_TIARA)) {
            return true;
        }

        BankEntity tiara = ctx.bank().withId(AIR_TIARA).first().orElse(null);
        if (tiara == null) {
            plugin.pauseScript("No air tiara in the bank, one is needed to enter the ruins");
            bankService.close();
            return false;
        }

        log.info("Withdrawing and equipping the air tiara");
        tiara.withdrawOne();

        BankInventoryEntity withdrawn = SleepService.sleepUntilNotNull(
                () -> ctx.bankInventory().withId(AIR_TIARA).first().orElse(null), ACTION_TIMEOUT_MS);

        if (withdrawn == null) {
            log.warn("Air tiara did not arrive in the inventory");
            return false;
        }

        withdrawn.wear();
        return SleepService.sleepUntil(() -> ctx.equipment().isWearing(AIR_TIARA), ACTION_TIMEOUT_MS);
    }

    @Override
    public String status() {
        return "Banking";
    }
}
