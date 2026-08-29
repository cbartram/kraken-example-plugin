package com.krakenplugins.example.fishing.script;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.script.PriorityTask;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.fishing.FishingConfig;
import com.krakenplugins.example.fishing.script.state.*;
import com.krakenplugins.example.fishing.script.state.barbarian.CookFish;
import com.krakenplugins.example.fishing.script.state.barbarian.FishBarbarianVillage;
import com.krakenplugins.example.fishing.script.state.corsair.BankCorsairCove;
import com.krakenplugins.example.fishing.script.state.corsair.FishCorsair;
import com.krakenplugins.example.fishing.script.state.corsair.WalkToCorsairBank;
import com.krakenplugins.example.fishing.script.state.corsair.WalkToResourceArea;
import com.krakenplugins.example.fishing.script.state.karamja.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Singleton
public class FishingScript extends Script {

    // Replaced from the client thread when the location config changes, read from the script thread.
    private volatile List<PriorityTask> tasks = new ArrayList<>();

    private final FishingConfig config;
    private final DropFish dropFish;
    private final FishKaramja fishKaramja;
    private final FishBarbarianVillage fishBarbarianVillage;
    private final FishDraynor fishDraynor;
    private final CookFish cookFish;
    private final TravelPortSarim travelPortSarim;
    private final BankDepositBox bankDepositBox;
    private final WalkToMusaPoint walkToMusaPoint;
    private final BankCorsairCove bankCorsairCove;
    private final FishCorsair fishCorsair;
    private final WalkToCorsairBank walkToCorsairBank;
    private final WalkToResourceArea walkToResourceArea;


    @Getter
    private volatile String status = "Initializing";

    @Inject
    public FishingScript(final FishingConfig config, final DropFish dropFish, final FishKaramja fishKaramja, final FishBarbarianVillage fishBarbarianVillage,
                         final FishDraynor fishDraynor, final CookFish cookFish, final TravelPortSarim travelPortSarim,
                         final BankDepositBox bankDepositBox, final WalkToMusaPoint walkToMusaPoint,
                         final BankCorsairCove bankCorsairCove, final FishCorsair fishCorsair, final WalkToCorsairBank walkToCorsairBank, final WalkToResourceArea walkToResourceArea) {
        this.config = config;
        this.dropFish = dropFish;
        this.fishKaramja = fishKaramja;
        this.fishBarbarianVillage = fishBarbarianVillage;
        this.fishDraynor = fishDraynor;
        this.cookFish = cookFish;
        this.travelPortSarim = travelPortSarim;
        this.bankDepositBox = bankDepositBox;
        this.walkToMusaPoint = walkToMusaPoint;
        this.bankCorsairCove = bankCorsairCove;
        this.fishCorsair = fishCorsair;
        this.walkToCorsairBank = walkToCorsairBank;
        this.walkToResourceArea = walkToResourceArea;
    }

    public void setTasksForLocation(FishingLocation location) {
        List<PriorityTask> tasks = new ArrayList<>();
        if(config.dropFish()) {
            tasks.add(dropFish);
        }

        switch (location) {
            case DRAYNOR_VILLAGE:
                tasks.add(fishDraynor);
                break;
            case KARAMJA:
                tasks.add(fishKaramja);
                // The banking tasks are always added. Reading the bank config inside validate() rather
                // than here means a config change is picked up on the next loop with no rebuild.
                tasks.add(travelPortSarim);
                tasks.add(bankDepositBox);
                tasks.add(walkToMusaPoint);
                break;
            case CORSAIR_COVE:
                tasks.add(fishCorsair);
                tasks.add(walkToCorsairBank);
                tasks.add(walkToResourceArea);
                tasks.add(bankCorsairCove);
                break;
            case BARBARIAN_VILLAGE:
                // Cook fish is always added; it reads the cook config inside validate().
                tasks.add(fishBarbarianVillage);
                tasks.add(cookFish);
                break;
            default:
                break;
        }

        tasks.sort(Comparator.comparingInt(PriorityTask::getPriority));
        this.tasks = tasks;
    }

    /**
     * Pauses the loop and leaves the reason on the overlay in place of the current task's status.
     */
    public void pause(String reason) {
        status = reason;
        pause();
    }

    @Override
    public int loop() {
        for (Task task : tasks) {
            if (task.validate()) {
                status = task.status();
                return task.execute();
            }
        }
        return 0;
    }
}
