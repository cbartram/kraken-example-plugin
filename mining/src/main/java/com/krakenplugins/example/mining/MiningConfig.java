
package com.krakenplugins.example.mining;

import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import net.runelite.client.config.*;

@ConfigGroup("autominer")
public interface MiningConfig extends Config {

	@ConfigSection(
			name = "Mouse",
			description = "Options for configuring mouse movements",
			position = 1
	)
	String mouse = "mouse";

	@ConfigItem(
			keyName = "mouseMovementStrategy",
			name = "Movement Strategy",
			description = "Determines which strategy is used to move the client's mouse.",
			position = 1,
			section = mouse
	)
	default MouseMovementStrategy mouseMovementStrategy() {
		return MouseMovementStrategy.BEZIER;
	}

	@ConfigItem(
			keyName = "replayLibrary",
			name = "Replay Library",
			description = "Determines which library to load when the mouse strategy is set to: REPLAY.",
			position = 2,
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
			position = 3,
			section = mouse
	)
	default int linearSteps() {
		return 150;
	}

	@ConfigSection(
			name = "Run Energy",
			description = "Run energy settings",
			position = 2
	)
	String run = "Run";

	@Range(min = 1, max = 99)
	@ConfigItem(
			keyName = "runEnergyThreshold",
			name = "Run Energy Min",
			description = "Toggles your run on at a random number between the min and max threshold set here.",
			position = 3,
			section = run
	)
	default int runEnergyThresholdMin() {
		return 70;
	}

	@Range(min = 1, max = 99)
	@ConfigItem(
			keyName = "runEnergyThresholdMax",
			name = "Run Energy Max",
			description = "Toggles your run on at a random number between the min and max threshold set here.",
			position = 4,
			section = run
	)
	default int runEnergyThresholdMax() {
		return 90;
	}

	@ConfigSection(
		name = "Overlays",
		description = "Script overlay options",
		position = 999
	)
	String overlay = "overlay";

	@ConfigItem(
		keyName = "highlightTargetRock",
		name = "Highlight Target Rock",
		description = "Highlights the selected rock to mine.",
		position = 1,
		section = overlay
	)
	default boolean highlightTargetRock() {
		return false;
	}

	@ConfigItem(
			keyName = "renderPath",
			name = "Render Path",
			description = "Show the computed path to your destination on the screen.",
			position = 2,
			section = overlay
	)
	default boolean renderPath() {
		return false;
	}

	@ConfigItem(
			keyName = "debug",
			name = "Debug",
			description = "Shows debug related overlays like specific areas of the game.",
			position = 3,
			section = overlay
	)
	default boolean debug() {
		return false;
	}

}
