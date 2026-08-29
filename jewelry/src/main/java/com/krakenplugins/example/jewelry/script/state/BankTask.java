package com.krakenplugins.example.jewelry.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;

import java.util.List;

import static com.krakenplugins.example.jewelry.script.JewelryScript.GOLD_BAR;

@Slf4j
@Singleton
public class BankTask extends AbstractTask {

    // 13 bars, 13 gems and the mould fill 27 of the 28 inventory slots.
    private static final int MATERIALS_PER_TRIP = 13;

    // How long the bank has to answer a deposit, a withdraw or a close before the loop retries it.
    private static final long BANK_TIMEOUT_MS = 3000;

    // Mean and deviation of the pause between the two withdrawals, in milliseconds.
    private static final int WITHDRAW_PAUSE_MEAN_MS = 1200;
    private static final int WITHDRAW_PAUSE_DEVIATION_MS = 200;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryConfig config;

    @Inject
    private JewelryPlugin plugin;

    @Override
    public boolean validate() {
        // An open bank interface at Edgeville means the trip is ours to finish, whatever state it is
        // in. Claiming every such state is what stops the script parking with the interface up.
        return bankService.isOpen() && ctx.players().local().isInArea(plugin.getEdgevilleBank());
    }

    @Override
    public int execute() {
        String necklaceName = config.jewelry().getNecklaceName();

        BankInventoryEntity necklace = ctx.bankInventory().withName(necklaceName).random();
        if (necklace != null) {
            // Any slot of the stack deposits all of them, so picking one at random keeps the click
            // off the same inventory square every trip.
            plugin.moveMouseTo(necklace.raw());
            necklace.depositAll();
            if (!SleepService.sleepUntil(() -> ctx.inventory().withName(necklaceName).isEmpty(), BANK_TIMEOUT_MS)) {
                log.info("Necklaces are still in the inventory after depositing, retrying");
                return 600;
            }
        }

        if (ctx.inventory().hasItems(GOLD_BAR, config.jewelry().getSecondaryGemId())) {
            // Materials are already in hand, so all that is left is to leave the interface closed.
            return closeBank();
        }

        if (!withdrawMould()) {
            return 600;
        }

        BankEntity goldBar = ctx.bank().withId(GOLD_BAR).first();
        BankEntity gem = ctx.bank().withId(config.jewelry().getSecondaryGemId()).first();
        if (goldBar == null || gem == null) {
            plugin.halt("The bank is out of " + (goldBar == null ? "gold bars" : "gems")
                    + (config.enableResupply() ? "" : " — turn Resupply on to buy more"));
            return 0;
        }

        // Withdrawing the two materials in the same order every trip is a pattern, so half the trips
        // take the gem first.
        List<BankEntity> order = RandomService.dicePercentage(50)
                ? List.of(goldBar, gem)
                : List.of(gem, goldBar);

        for (BankEntity material : order) {
            // A retry after a half-finished trip must not withdraw a second helping of what is
            // already in the inventory.
            if (ctx.inventory().hasItem(material.getId())) {
                continue;
            }

            if (!material.withdraw(MATERIALS_PER_TRIP)) {
                log.info("Withdraw of {} was not dispatched, retrying", material.getName());
                return 600;
            }
            SleepService.sleepGaussian(WITHDRAW_PAUSE_MEAN_MS, WITHDRAW_PAUSE_DEVIATION_MS);
        }

        plugin.getMetrics().setGoldBarsRemaining(Math.max(goldBar.count() - MATERIALS_PER_TRIP, 0));
        plugin.getMetrics().setGemsRemaining(Math.max(gem.count() - MATERIALS_PER_TRIP, 0));

        return closeBank();
    }

    /**
     * Makes sure the necklace mould is in the inventory before any materials are withdrawn.
     *
     * @return true when the mould is in hand or on its way, false when the caller should wait. The
     *         script is halted outright when there is no mould to be had at all.
     */
    private boolean withdrawMould() {
        if (ctx.bankInventory().withId(ItemID.NECKLACE_MOULD).isPresent()) {
            return true;
        }

        BankEntity mould = ctx.bank().withId(ItemID.NECKLACE_MOULD).first();
        if (mould == null) {
            plugin.halt("No necklace mould in the inventory or the bank");
            return false;
        }

        if (!mould.withdrawOne()) {
            log.info("Withdraw of the necklace mould was not dispatched, retrying");
            return false;
        }

        return SleepService.sleepUntil(
                () -> ctx.bankInventory().withId(ItemID.NECKLACE_MOULD).isPresent(), BANK_TIMEOUT_MS);
    }

    /**
     * Leaves the bank interface closed for the tasks that walk off to the furnace.
     *
     * @return The number of milliseconds the loop should sleep for.
     */
    private int closeBank() {
        bankService.close();
        if (!SleepService.sleepUntil(bankService::isClosed, BANK_TIMEOUT_MS)) {
            log.info("The bank interface is still open, retrying");
        }
        return 600;
    }

    @Override
    public String status() {
        return "Banking";
    }
}
