
package com.krakenplugins.example;

import net.runelite.client.config.*;

@ConfigGroup("autominer")
public interface MiningConfig extends Config {

	@ConfigSection(
		name = "General",
		description = "",
		position = 1
	)
	String general = "General";

	@ConfigItem(
		keyName = "highlightTargetRock",
		name = "Highlight Target Rock",
		description = "Highlights the selected rock to mine.",
		position = 1,
		section = general
	)
	default boolean highlightTargetRock() {
		return false;
	}

	@Range(min = 1, max = 99)
	@ConfigItem(
			keyName = "runEnergyThreshold",
			name = "Run Energy Min",
			description = "Toggles your run on at a random number between the min and max threshold set here.",
			position = 2,
			section = general
	)
	default int runEnergyThresholdMin() {
		return 70;
	}

	@Range(min = 1, max = 99)
	@ConfigItem(
			keyName = "runEnergyThresholdMax",
			name = "Run Energy Max",
			description = "Toggles your run on at a random number between the min and max threshold set here.",
			position = 3,
			section = general
	)
	default int runEnergyThresholdMax() {
		return 90;
	}
}
