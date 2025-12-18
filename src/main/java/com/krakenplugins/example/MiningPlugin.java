package com.krakenplugins.example;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.script.Script;
import com.kraken.api.overlay.MouseOverlay;
import com.kraken.api.service.util.SleepService;
import com.krakenplugins.example.overlay.MovementOverlay;
import com.krakenplugins.example.overlay.SceneOverlay;
import com.krakenplugins.example.overlay.ScriptOverlay;
import com.krakenplugins.example.script.Task;
import com.krakenplugins.example.script.state.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
@PluginDescriptor(
        name = "Mining Example Plugin",
        enabledByDefault = false,
        description = "Demonstrates an example of building a Mining automation plugin using the Kraken API.",
        tags = {"example", "automation", "kraken"}
)
public class MiningPlugin extends Plugin {

    public static final WorldPoint BANK_LOCATION = new WorldPoint(3253, 3421, 0);
    public static final WorldPoint MINE_LOCATION = new WorldPoint(3287, 3367, 0);
    public static final int IRON_ORE_GAME_OBJECT = 11365;
    public static final int BANK_BOOTH_GAME_OBJECT = 10583;

    @Inject
    private Script script;

    @Inject
    private Context ctx;

    @Getter
    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ScriptOverlay scriptOverlay;

    @Inject
    private MouseOverlay mouseTrackerOverlay;

    @Inject
    private MovementOverlay movementOverlay;

    @Inject
    private SceneOverlay sceneOverlay;

    @Inject
    private SleepService sleepService;

    @Inject
    private MiningTask miningTask;

    @Inject
    private BankingTask bankingTask;

    @Inject
    private OpenBankTask openBankTask;

    @Inject
    private WalkToMineTask walkToMineTask;

    @Inject
    private WalkToBankTask walkToBankTask;

    @Inject
    private FollowPathTask followPathTask;

    private final List<Task> tasks = new ArrayList<>();
    private long startTime;

    @Getter
    private String status = "Initializing";

    @Getter
    @Setter
    private GameObject targetRock;

    @Getter
    private final List<WorldPoint> currentPath = new ArrayList<>();

    @Provides
    MiningConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(MiningConfig.class);
    }

    @Override
    protected void startUp() {
        ctx.initializePackets();
        script.setLoopTask(() -> {
            for (Task task : tasks) {
                if (task.validate()) {
                    status = task.status();
                    int delay = task.execute();
                    if (delay > 0) {
                        sleepService.sleep(delay);
                    }
                    break; // Execute only the highest priority valid task
                }
            }
        });

        overlayManager.add(scriptOverlay);
        overlayManager.add(mouseTrackerOverlay);
        overlayManager.add(sceneOverlay);
        overlayManager.add(movementOverlay);

        startTime = System.currentTimeMillis();
        tasks.clear();
        tasks.addAll(List.of(
                followPathTask,
                miningTask,
                bankingTask,
                openBankTask,
                walkToMineTask,
                walkToBankTask
        ));
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(scriptOverlay);
        overlayManager.remove(mouseTrackerOverlay);
        overlayManager.remove(sceneOverlay);
    }

    @Subscribe
    private void onGameTick(GameTick e) {
        script.onGameTick(e);
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        final GameState gameState = event.getGameState();
        switch (gameState) {
            case LOGGED_IN:
                startUp();
                break;
            case HOPPING:
            case LOGIN_SCREEN:
                shutDown();
            default:
                break;
        }
    }

    public String getRuntime() {
        long millis = System.currentTimeMillis() - startTime;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    public int getOreMined() {
        return 0; // Placeholder: Implement tracking logic
    }
}
