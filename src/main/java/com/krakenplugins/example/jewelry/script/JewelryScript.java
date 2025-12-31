package com.krakenplugins.example.jewelry.script;

import com.google.inject.Inject;
import com.kraken.api.core.script.Script;
import com.kraken.api.core.script.Task;
import com.kraken.api.service.tile.AreaService;
import com.kraken.api.service.tile.GameArea;
import com.krakenplugins.example.jewelry.script.state.BankTask;
import com.krakenplugins.example.jewelry.script.state.CraftTask;
import com.krakenplugins.example.jewelry.script.state.OpenBankTask;
import com.krakenplugins.example.jewelry.script.state.OpenFurnaceTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class JewelryScript extends Script {

    public static final int GOLD_BAR = 2357;
    public static final int FURNACE_GAME_OBJECT = 16469;
    public static final int BANK_BOOTH_ID = 10355;

    @Getter
    public final GameArea edgevilleFurnace;

    @Getter
    public final GameArea edgevilleBank;

    private final List<Task> tasks;

    @Getter
    private String status = "Initializing";

    @Inject
    public JewelryScript(BankTask bankTask, CraftTask craftTask, OpenBankTask openBankTask, OpenFurnaceTask openFurnaceTask, AreaService areaService) {
        WorldPoint[] furnace = {
                new WorldPoint(3105, 3502, 0),
                new WorldPoint(3105, 3496, 0),
                new WorldPoint(3111, 3496, 0),
                new WorldPoint(3111, 3502, 0)
        };
        edgevilleFurnace = areaService.createPolygonArea(Arrays.asList(furnace));
        WorldPoint[] bank = {
                new WorldPoint(3091, 3500, 0),
                new WorldPoint(3089, 3496, 0),
                new WorldPoint(3091, 3492, 0),
                new WorldPoint(3091, 3487, 0),
                new WorldPoint(3099, 3487, 0),
                new WorldPoint(3099, 3500, 0)
        };
        edgevilleBank = areaService.createPolygonArea(Arrays.asList(bank));
        this.tasks = List.of(
                openBankTask,
                openFurnaceTask,
                bankTask,
                craftTask
        );
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
