package com.krakenplugins.autorunecrafting.script.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.AbstractTask;
import com.kraken.api.query.container.bank.BankEntity;
import com.kraken.api.query.container.bank.BankInventoryEntity;
import com.kraken.api.query.equipment.EquipmentEntity;
import com.kraken.api.service.bank.BankService;
import com.kraken.api.service.util.RandomService;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.autorunecrafting.AutoRunecraftingConfig;
import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;

import static com.krakenplugins.autorunecrafting.script.RunecraftingScript.*;

@Slf4j
@Singleton
public class BankTask extends AbstractTask {

    @Inject
    private AutoRunecraftingPlugin plugin;

    @Inject
    private AutoRunecraftingConfig config;

    @Inject
    private BankService bankService;

    @Override
    public boolean validate() {
        return ctx.players().local().isIdle() && ctx.players().local().isInArea(plugin.getFaladorBank()) &&
                !ctx.inventory().hasItem(PURE_ESSENCE) && !ctx.inventory().hasItem(RUNE_ESSENCE) && bankService.isOpen();
    }

    @Override
    public int execute() {
        plugin.getCurrentPath().clear();
        BankInventoryEntity craftedRunes = ctx.bankInventory()
                .nameContains("rune")
                .random();
        if (craftedRunes != null) {
            if (config.useMouse()) {
                ctx.getMouse().move(craftedRunes.raw());
            }
            craftedRunes.depositAll();
            SleepService.sleepUntil(() -> ctx.inventory().nameContains("rune").stream().findAny().isEmpty(), 3000);
        }

        BankInventoryEntity inventoryTiara = ctx.bankInventory().nameContains("tiara").first();
        if(inventoryTiara != null) {
            log.info("Equipping Air tiara...");
            inventoryTiara.wear();
            SleepService.sleep(2);
        }

        EquipmentEntity headSlot = ctx.equipment().inInterface().inSlot(EquipmentInventorySlot.HEAD);

        if(headSlot == null || headSlot.getId() != AIR_TIARA) {
            BankEntity tiara = ctx.bank().withId(AIR_TIARA).first();
            if(tiara == null) {
                log.error("Player does not have air tiara in their bank. Cannot craft runes.");
                return 600;
            }

            tiara.withdrawOne();
            SleepService.sleep(4);
            BankInventoryEntity newTiara = ctx.bankInventory().nameContains("tiara").first();

            if(newTiara != null) {
                newTiara.wear();
            } else {
                log.error("No equipment found in inventory with name a name containing: \"tiara\"");
            }
        }


        BankEntity pureEssence = ctx.bank().withId(PURE_ESSENCE).first();
        if (pureEssence != null) {
            log.info("Withdrawing Pure Essence");
            pureEssence.withdrawAll();
        } else {
            log.info("Pure essence not found in bank. Using rune essence");
            BankEntity runeEssence = ctx.bank().withId(RUNE_ESSENCE).first();
            if (runeEssence != null) {
                log.info("Withdrawing Rune Essence");
                runeEssence.withdrawAll();
            } else {
                log.error("No pure or rune essence found in bank. Cannot craft runes.");
                return 2400;
            }
        }

        // We don't close the bank because the Walk to altar task will check everything and close the bank for us
        return RandomService.between(750, 1120);
    }

    @Override
    public String status() {
        return "Banking";
    }
}
