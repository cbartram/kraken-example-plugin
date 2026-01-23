package com.krakenplugins.example.jewelry;

import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import com.krakenplugins.example.jewelry.script.Jewelry;
import net.runelite.client.config.*;

@ConfigGroup("autojewelry")
public interface JewelryConfig extends Config {

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
			name = "Jewelry Crafting",
			description = "Jewelry Crafting options",
			position = 2
	)
	String crafting = "crafting";

	@ConfigItem(
			keyName = "jewelry",
			name = "Jewelry",
			description = "Sets the jewelry to craft.",
			position = 1,
			section = crafting
	)
	default Jewelry jewelry() {
		return Jewelry.SAPPHIRE_NECKLACE;
	}

	@ConfigItem(
			keyName = "jewelry",
			name = "Jewelry",
			description = "Sets the jewelry to craft.",
			position = 1,
			section = crafting
	)
	default Jewelry jewelry() {
		return Jewelry.SAPPHIRE_NECKLACE;
	}

	@ConfigSection(
			name = "Resupply",
			description = "Options for configuring how the script resupplies materials when out.",
			position = 3
	)
	String resupply = "resupply";

	@Range(min = 1, max = Integer.MAX_VALUE)
	@ConfigItem(
			keyName = "coins",
			name = "Max coins to use",
			description = "The maximum amount of coins that will be withdrawn to resupply.",
			position = 1,
			section = resupply
	)
	default int maxCoins() {
		return 300000;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "purchaseBufferPercent",
			name = "Purchase Buffer %",
			description = "A percentage representing the maximum increase in price that can be used to purchase materials <br>" +
					"For example 5, would mean that the script may try to purchase materials for 5% over the actively traded price.",
			position = 2,
			section = resupply
	)
	default int purchaseBufferPercent() {
		return 3;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "sellBufferPercent",
			name = "Sell Buffer %",
			description = "A percentage representing the maximum reduction in price that can be used to sell jewelry <br>" +
					"For example 5, would mean that the script may try to sell jewelry for 5% under the actively traded price.",
			position = 3,
			section = resupply
	)
	default int sellBufferPercent() {
		return 3;
	}

	@ConfigSection(
			name = "Overlays",
			description = "Script overlay options",
			position = 999
	)
	String overlay = "overlay";

	@ConfigItem(
			keyName = "targetBankBooth",
			name = "Show Target Bank Booth",
			description = "Show the target bank booth.",
			position = 1,
			section = overlay
	)
	default boolean targetBankBooth() {
		return false;
	}

	@ConfigItem(
			keyName = "showCurrentPath",
			name = "Show Current Path",
			description = "Show the currently calculated path.",
			position = 2,
			section = overlay
	)
	default boolean showCurrentPath() {
		return false;
	}

	@ConfigItem(
			keyName = "debug",
			name = "Debug",
			description = "Show debug information and overlays.",
			position = 3,
			section = overlay
	)
	default boolean debug() {
		return false;
	}
}
