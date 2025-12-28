package com.krakenplugins.example.fishing.script;


import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.fishing.script.state.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FishingScript extends Script {

    private List<Task> tasks = new ArrayList<>();
    private final DropFish dropFish;
    private final FishKaramja fishKaramja;
    private final FishBarbarianVillage fishBarbarianVillage;
    private final FishDraynor fishDraynor;
    private final CookFish cookFish;

    @Getter
    private String status = "Initializing";

    @Inject
    public FishingScript(final DropFish dropFish, final FishKaramja fishKaramja, final FishBarbarianVillage fishBarbarianVillage,
                         final FishDraynor fishDraynor, final CookFish cookFish) {
        this.dropFish = dropFish;
        this.fishKaramja = fishKaramja;
        this.fishBarbarianVillage = fishBarbarianVillage;
        this.fishDraynor = fishDraynor;
        this.cookFish = cookFish;
    }

    public void setTasksForLocation(FishingLocation location) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(dropFish);
        switch (location) {
            case DRAYNOR_VILLAGE:
                tasks.add(fishDraynor);
                break;
            case KARAMJA:
                tasks.add(fishKaramja);
                break;
            case BARBARIAN_VILLAGE:
                // Safe to always add cook fish task regardless of user config since the activate() method checks
                // config before executing cook fish task.
                tasks.add(fishBarbarianVillage);
                tasks.add(cookFish);
                break;
            default:
                break;
        }

        this.tasks = tasks;
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
