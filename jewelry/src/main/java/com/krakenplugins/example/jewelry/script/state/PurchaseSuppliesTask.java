package com.krakenplugins.example.jewelry.script.state;

import com.kraken.api.Context;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.query.container.inventory.InventoryEntity;
import com.kraken.api.query.npc.NpcEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.ui.grandexchange.GrandExchangeService;
import com.kraken.api.service.ui.grandexchange.GrandExchangeSlot;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.jewelry.JewelryConfig;
import com.krakenplugins.example.jewelry.JewelryPlugin;
import com.krakenplugins.example.jewelry.script.ItemPrice;
import com.krakenplugins.example.jewelry.script.JewelryScript;
import com.krakenplugins.example.jewelry.script.PriceManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class PurchaseSuppliesTask extends AbstractTask {

    @Inject
    private GrandExchangeService geService;

    @Inject
    private BankService bankService;

    @Inject
    private JewelryPlugin plugin;

    @Inject
    private Context ctx;

    @Inject
    private JewelryConfig config;

    @Inject
    private PriceManager priceManager;

    private long lastPurchaseAttemptTime = -1;
    private int bankGoldBars = 0;
    private int bankGems = 0;

    @Override
    public boolean validate() {
        if (lastPurchaseAttemptTime != -1 && System.currentTimeMillis() - lastPurchaseAttemptTime > 50000) {
            log.info("Resetting GE Purchase Attempt Time");
            lastPurchaseAttemptTime = -1;
            return false;
        }

        return ctx.players().local().isInArea(plugin.getGrandExchange()) && lastPurchaseAttemptTime == -1;
    }

    @Override
    public int execute() {
        log.info("Attempting to purchase");
        try {
            // 1. Prepare Bank: Check supplies, withdraw crafted items to sell, withdraw coins
            if (!prepareBank()) {
                log.error("Failed to prepare bank");
                return 600;
            }

            NpcEntity clerk = ctx.npcs().withAction("Exchange").nearest();
            if (clerk == null) {
                log.error("Could not find Grand Exchange Clerk");
                return 600;
            }

            // 2. Open GE
            if (!clerk.interact("Exchange")) {
                log.error("Failed to interact with Grand Exchange Clerk");
                return 600;
            }

            SleepService.sleepUntil(geService::isOpen, 5000);

            // Wait several ticks before attempting to sell
            SleepService.sleepFor(3);
            sellCraftedItems();
            buySupplies();
            depositAll();

            lastPurchaseAttemptTime = System.currentTimeMillis();
            return 0;
        } catch (Exception e) {
            log.error("Failed to resupply: ", e);
        } finally {
            lastPurchaseAttemptTime = System.currentTimeMillis();
        }
        return 0;
    }

    private boolean prepareBank() {
        NpcEntity banker = ctx.npcs().withAction("Bank").nearest();
        if (banker == null) {
            log.error("Cannot find banker");
            return false;
        }

        if (!banker.interact("Bank")) {
            log.error("Failed to interact with banker");
            return false;
        }

        SleepService.sleepUntil(bankService::isOpen, 5000);

        if (!ctx.inventory().isEmpty()) {
            bankService.depositAll();
            SleepService.sleepFor(1);
        }

        // Check supplies in bank
        BankEntity goldBars = ctx.bank().withId(JewelryScript.GOLD_BAR).first();
        bankGoldBars = goldBars != null ? goldBars.count() : 0;

        BankEntity gems = ctx.bank().withId(config.jewelry().getSecondaryGemId()).first();
        bankGems = gems != null ? gems.count() : 0;

        // Withdraw crafted jewelry (Noted)
        int craftedId = config.jewelry().getCraftedItemId();
        BankEntity crafted = ctx.bank().withId(craftedId).first();
        if (crafted != null && crafted.count() > 0) {
            crafted.withdrawAllNoted();
            SleepService.sleepFor(1);
        }

        // Withdraw Coins
        BankEntity coins = ctx.bank().withId(995).first();
        BankInventoryEntity inventoryCoins = ctx.bankInventory().withId(995).first();

        if(inventoryCoins != null) {
            int value = inventoryCoins.raw().getQuantity();
            if(value > config.maxCoins()) {
                log.info("Already have enough coins in inventory.");
                bankService.close();
                SleepService.sleepUntil(() -> !bankService.isOpen(), 3000);
                return true;
            } else {
                log.info("Have {} coins in inventory, need to withdraw {} to hit max", value, config.maxCoins() - value);
            }
        }

        if (coins != null) {
            int currentCoins;
            if(inventoryCoins == null) {
                currentCoins = 0;
            } else {
                currentCoins = inventoryCoins.raw().getQuantity();
            }
            
            int needed = config.maxCoins() - currentCoins;
            if (needed > 0) {
                int toWithdraw = Math.min(coins.count(), needed);
                log.info("Need to withdraw: {} coins to hit max", toWithdraw);
                coins.withdraw(toWithdraw);
                SleepService.sleepFor(3);
            }
        } else {
            log.warn("No coins found in bank!");
        }

        bankService.close();
        SleepService.sleepUntil(() -> !bankService.isOpen(), 3000);
        return true;
    }

    private void sellCraftedItems() {
        int craftedId = config.jewelry().getCraftedItemId(); // Add 1 to make it noted since it will be in noted form.
        int notedCraftedId = craftedId + 1;
        InventoryEntity craftedItem = ctx.inventory().withId(notedCraftedId).first();

        if (craftedItem != null) {
            int price = getMinSellPrice(craftedId);
            GrandExchangeSlot slot = geService.queueSellOrder(notedCraftedId, price);
            if (slot != null) {
                log.info("Selling crafted items at price: {}", price);
                while(!slot.isFulfilled()) {
                    SleepService.tick();
                }

                log.info("Item sold");
                SleepService.sleepFor(1);
                geService.collect(slot, false); // Collect coins
                SleepService.sleepFor(1);
            } else {
                log.info("GE Slot is null, ensure there is a free slot available");
            }
        } else {
            log.info("No crafted items in inventory to sell.");
        }
    }

    private void buySupplies() {
        int goldBarPrice = getMaxBuyPrice(JewelryScript.GOLD_BAR);
        int gemPrice = getMaxBuyPrice(config.jewelry().getSecondaryGemId());

       InventoryEntity coins = ctx.inventory().withId(995).first();

       int currentCoins;
       if(coins == null) {
           currentCoins = 0;
       } else {
           currentCoins = coins.raw().getQuantity();
       }
        
        // Calculate how many to buy to balance and fill up
        int toBuyGold = 0;
        int toBuyGems = 0;

        // Balance first
        if (bankGoldBars < bankGems) {
            int diff = bankGems - bankGoldBars;
            int cost = diff * goldBarPrice;
            if (currentCoins >= cost) {
                toBuyGold += diff;
                currentCoins -= cost;
                bankGoldBars += diff;
            } else {
                toBuyGold += currentCoins / goldBarPrice;
                currentCoins = 0;
            }

            log.info("Have more gold bars than gems, buying {} gold bars, est cost: {}", toBuyGold, cost);
        } else if (bankGems < bankGoldBars) {
            int diff = bankGoldBars - bankGems;
            int cost = diff * gemPrice;
            if (currentCoins >= cost) {
                toBuyGems += diff;
                currentCoins -= cost;
                bankGems += diff;
            } else {
                toBuyGems += currentCoins / gemPrice;
                currentCoins = 0;
            }
            log.info("Have more gems than gold bars, buying {} gems, est cost: {}", toBuyGems, cost);
        } else {
            log.info("Banked gold bars: {}, banked gems: {}", bankGoldBars, bankGems);
        }

        // Buy pairs with remaining coins
        if (currentCoins > 0) {
            int pairCost = goldBarPrice + gemPrice;
            int pairs = currentCoins / pairCost;
            toBuyGold += pairs;
            toBuyGems += pairs;
        }

        log.info("Buying {} Gold Bars@{} and {} Gems@{}", toBuyGold, goldBarPrice, toBuyGems, gemPrice);

        GrandExchangeSlot goldSlot = null;
        GrandExchangeSlot gemSlot = null;

        if (toBuyGold > 0) {
            goldSlot = geService.queueBuyOrder(JewelryScript.GOLD_BAR, toBuyGold, goldBarPrice);
            SleepService.sleepFor(3);
        }
        
        if (toBuyGems > 0) {
            gemSlot = geService.queueBuyOrder(config.jewelry().getSecondaryGemId(), toBuyGems, gemPrice);
            SleepService.sleepFor(3);
        }

        // Wait for both
        long start = System.currentTimeMillis();
        boolean goldDone = (goldSlot == null);
        boolean gemDone = (gemSlot == null);

        while ((!goldDone || !gemDone) && System.currentTimeMillis() - start < 60000) {
            if (!goldDone && goldSlot.isFulfilled()) goldDone = true;
            if (!gemDone && gemSlot.isFulfilled()) gemDone = true;
            SleepService.sleep(500);
        }

        if (goldSlot != null) {
             if (!goldSlot.isFulfilled()) geService.cancelOffer(goldSlot);
             geService.collect(goldSlot, true);
        }
        if (gemSlot != null) {
            if (!gemSlot.isFulfilled()) geService.cancelOffer(gemSlot);
            geService.collect(gemSlot, true);
        }
    }

    private void depositAll() {
        NpcEntity banker = ctx.npcs().withAction("Bank").nearest();
        if (banker != null && banker.interact("Bank")) {
            SleepService.sleepUntil(bankService::isOpen, 5000);
            if (bankService.isOpen()) {
                bankService.depositAll();
                bankService.close();
            }
        }
    }

    private int getMaxBuyPrice(int itemId) {
        double percentBuffer = (double) config.purchaseBufferPercent() / 100;
        try {
            ItemPrice price = priceManager.getItemPrice(itemId);
            int diff = price.getLow() + ((price.getHigh() - price.getLow()) / 2);
            int increase = (int) (diff * percentBuffer);
            log.info("Found buy price {}: {} buy over increase is: {}", diff + increase, price, increase);
            return diff + increase;
        } catch (Exception e) {
            log.error("Failed to lookup price on item: {}", itemId, e);
        }
        return 1;
    }

    private int getMinSellPrice(int itemId) {
        double percentBuffer = (double) config.sellBufferPercent() / 100;
        log.info("Looking up item price...");
        try {
            ItemPrice price = priceManager.getItemPrice(itemId);

            // We already factor in a reduction in price % based so go low add the average value then compute the difference buffer
            int diff = price.getLow() + ((price.getHigh() - price.getLow()) / 2);
            int increase = (int) (diff * percentBuffer);
            log.info("Calculated sell price {}: {} price sale reduction is: {}", diff - increase, price, increase);
            return diff - increase;
        } catch (Exception e) {
            log.error("Failed to lookup price on item {}: ", itemId, e);
            return 0;
        }
    }

    @Override
    public String status() {
        return "Purchasing Supplies";
    }
}
