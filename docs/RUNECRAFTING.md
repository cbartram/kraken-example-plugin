# Runecrafting Plugin

The Runecrafting Plugin is an example automation script built using the Kraken API. 
It automates the process of crafting Air runes at the Air Altar, demonstrating interaction with game objects, banking, and inventory management.

## Features
- Automates crafting Air runes.
- Banks at Falador Bank when inventory is empty of essence.
- Withdraws Rune or Pure essence automatically.
- Handles pathfinding and walking between the bank and the altar.

## Requirements & Usage
To use this plugin effectively, please ensure the following:

1. **Tiara**: You must have an **Air tiara** in your inventory.
2. **Starting Location**: Your character must be located at **Falador Bank**.
3. **Materials**: Ensure you have plenty of **Rune essence** or **Pure essence** in your bank.

## Limitations
- **Location**: This plugin is specifically designed for **Falador** and the **Air Altar**.
- **Runes**: Currently hardcoded to craft **Air Runes**.

## Running the Plugin
To run the runecrafting plugin, use the following commands in your terminal:

```shell
./gradlew build

java -jar ./runecrafting/build/libs/runecrafting-1.0.0.jar --developer-mode com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin
```

## Configuration
The plugin offers several configuration options to customize its behavior.

### Mouse Settings
* **Use Mouse Movement**: When true, the mouse will be moved on the canvas.
* **Movement Strategy**: Determines how the mouse moves. Options include `BEZIER` (default), `LINEAR`, etc.
* **Replay Library**: If `REPLAY` strategy is chosen, this defines which library to load.
* **Linear Steps**: If `LINEAR` strategy is chosen, this sets the number of steps (points) along the path (5-3000).

### Overlays
* **Show Target Bank Booth**: Show the target bank booth.
* **Show Current Path**: Show the currently calculated path.
* **Debug**: Show debug information and overlays.
