package com.krakenplugins.example.woodcutting;


import com.kraken.api.input.mouse.strategy.MouseMovementStrategy;
import net.runelite.client.config.*;

@ConfigGroup("autochopper")
public interface WoodcuttingConfig extends Config {

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
			name = "Tree",
			description = "Tree chopping settings",
			position = 2
	)
	String tree = "tree";

	@ConfigItem(
			keyName = "treeName",
			name = "Tree Name",
			description = "Configures the script to search for tress of this type to chop.",
			position = 1,
			section = tree
	)
	default String treeName() {
		return "Willow Tree";
	}

	@Range(min=1, max=100)
	@ConfigItem(
			keyName = "treeRadius",
			name = "Tree Radius",
			description = "Configures how far the script should look for choppable trees from the players location.",
			position = 2,
			section = tree
	)
	default int treeRadius() {
		return 8;
	}

	@ConfigSection(
		name = "Overlays",
		description = "Script overlay options",
		position = 999
	)
	String overlay = "overlay";

	@ConfigItem(
		keyName = "highlightTargetTree",
		name = "Highlight Target Tree",
		description = "Highlights the selected tree to chop.",
		position = 1,
		section = overlay
	)
	default boolean highlightTargetTree() {
		return false;
	}

	@ConfigItem(
		keyName = "showTreeRaadius",
		name = "Show Tree Radius",
		description = "Shows the radius of choppable trees from the players location.",
		position = 2,
		section = overlay
	)
	default boolean showTreeRadius() {
		return false;
	}

	@ConfigItem(
			keyName = "renderPath",
			name = "Render Path",
			description = "Show the computed path to your destination on the screen.",
			position = 3,
			section = overlay
	)
	default boolean renderPath() {
		return false;
	}


	@ConfigItem(
			keyName = "debug",
			name = "Debug",
			description = "Show debug information and overlays.",
			position = 4,
			section = overlay
	)
	default boolean debug() {
		return false;
	}

}
