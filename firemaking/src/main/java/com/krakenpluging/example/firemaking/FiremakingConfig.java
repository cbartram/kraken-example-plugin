package com.krakenpluging.example.firemaking;

import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import net.runelite.client.config.*;

@ConfigGroup("autofiremaker")
public interface FiremakingConfig extends Config {

    @ConfigSection(
			name = "Bank",
			description = "Options for configuring banking",
			position = -1
	)
	String bank = "bank";

	@ConfigItem(
			keyName = "bankPin",
			name = "Bank Pin",
			description = "When this field is not blank, it will enter the bank pin automatically for you. <br>" +
					"This should be a 4 digit number.",
			position = 1,
			section = bank
	)
	default String bankPin() {
		return "";
	}

    @ConfigSection(
            name = "Mouse",
            description = "Options for configuring mouse movements",
            position = 1
    )
    String mouse = "mouse";

    @ConfigItem(
            keyName = "useMouse",
            name = "Use Mouse Movement",
            description = "When true the mouse will be moved on the canvas.",
            position = 1,
            section = mouse
    )
    default boolean useMouse() {
        return false;
    }

    @ConfigItem(
            keyName = "mouseMovementStrategy",
            name = "Movement Strategy",
            description = "Determines which strategy is used to move the client's mouse.",
            position = 2,
            section = mouse
    )
    default MouseMovementStrategy mouseMovementStrategy() {
        return MouseMovementStrategy.BEZIER;
    }

    @ConfigItem(
            keyName = "replayLibrary",
            name = "Replay Library",
            description = "Determines which library to load when the mouse strategy is set to: REPLAY.",
            position = 3,
            section = mouse
    )
    default String replayLibrary() {
        return "";
    }

    @Range(min = 5, max = 3000)
    @ConfigItem(
            keyName = "linearSteps",
            name = "Linear Steps",
            description = "Determines how many steps (points along the linear path) should be generated when the mouse strategy is set to: LINEAR.",
            position = 4,
            section = mouse
    )
    default int linearSteps() {
        return 150;
    }

    @ConfigSection(
            name = "Logs",
            description = "Log settings",
            position = 2
    )
    String logs = "logs";

    @ConfigItem(
            keyName = "logName",
            name = "Log Name",
            description = "The name of the logs to burn.",
            position = 1,
            section = logs
    )
    default String logName() {
        return "Willow logs";
    }

    @ConfigSection(
            name = "Overlays",
            description = "Script overlay options",
            position = 999
    )
    String overlay = "overlay";

    @ConfigItem(
            keyName = "renderBanker",
            name = "Show Target Banker",
            description = "Show the selected banker to interact with.",
            position = 1,
            section = overlay
    )
    default boolean showBanker() {
        return false;
    }

    @ConfigItem(
            keyName = "showTargetFire",
            name = "Show Target Fire",
            description = "Highlight the selected fire for the bonfire.",
            position = 2,
            section = overlay
    )
    default boolean showTargetFire() {
        return false;
    }

    @ConfigItem(
            keyName = "debug",
            name = "Debug",
            description = "Show debug information (script area checks, etc...) and overlays.",
            position = 2,
            section = overlay
    )
    default boolean debug() {
        return false;
    }
}
