package com.krakenplugins.example.fishing.script;


import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.krakenplugins.example.fishing.script.state.DropFish;
import com.krakenplugins.example.fishing.script.state.FishBarbarianVillage;
import com.krakenplugins.example.fishing.script.state.FishDraynor;
import com.krakenplugins.example.fishing.script.state.FishKaramja;
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

    @Getter
    private String status = "Initializing";

    @Inject
    public FishingScript(final DropFish dropFish, final FishKaramja fishKaramja, final FishBarbarianVillage fishBarbarianVillage, final FishDraynor fishDraynor) {
        this.dropFish = dropFish;
        this.fishKaramja = fishKaramja;
        this.fishBarbarianVillage = fishBarbarianVillage;
        this.fishDraynor = fishDraynor;
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
                tasks.add(fishBarbarianVillage);
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
