package com.krakenplugins.example.mining.script.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.mining.MiningPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;

@Slf4j
@Singleton
public class BankingTask extends AbstractTask {

    // How long the deposit has to clear the inventory before the loop retries it.
    private static final long DEPOSIT_TIMEOUT_MS = 3000;

    @Inject
    private BankService bankService;

    @Inject
    private MiningPlugin plugin;

    @Override
    public boolean validate() {
        // An open bank interface already means we are standing at a booth.
        return bankService.isOpen() && ctx.inventory().isFull();
    }

    @Override
    public int execute() {
        BankInventoryEntity iron = ctx.bankInventory().withId(ItemID.IRON_ORE).first();
        if (iron == null) {
            plugin.halt("Inventory is full but holds no iron ore to deposit");
            return 0;
        }

        plugin.moveMouseTo(iron.raw());
        iron.depositAll();

        if (!SleepService.sleepUntil(() -> ctx.inventory().withId(ItemID.IRON_ORE).isEmpty(), DEPOSIT_TIMEOUT_MS)) {
            log.info("Iron ore is still in the inventory after depositing, retrying");
            return 600;
        }

        // The next task walks off to the mine, so leave the interface closed behind us.
        bankService.close();
        SleepService.sleepUntil(bankService::isClosed, DEPOSIT_TIMEOUT_MS);
        return 600;
    }

    @Override
    public String status() {
        return "Depositing Ore";
    }
}
