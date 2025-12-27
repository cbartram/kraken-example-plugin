# Woodcutting Plugin

The Woodcutting Plugin is an example automation script built using the Kraken API. It automates the process of chopping trees and banking logs, demonstrating interaction with game objects, inventory management, and configuration settings.

## Features
- Automates chopping trees near Draynor Village.
- Configurable tree type (e.g., Oak, Willow, Regular).
- Banks logs at Draynor Bank when the inventory is full.
- Handles pathfinding and walking between the bank and the trees.

## Requirements & Usage
To use this plugin effectively, please ensure the following:

1. **Axe**: You must have an axe equipped or in your inventory that matches your woodcutting level.
2. **Starting Location**: Your character must be located at **Draynor Bank**.
3. **Inventory**: Start with an empty inventory (except for your axe if it is not equipped).

## Limitations
- **Location**: This plugin is specifically designed for **Draynor Village** (Draynor Bank and surrounding trees). It does not support other locations.

## Running the Plugin
To run the woodcutting plugin, use the following commands in your terminal:

```shell
./gradlew build

java -jar ./build/libs/kraken-example-plugin-1.0.0.jar --developer-mode com.krakenplugins.example.woodcutting.WoodcuttingPlugin
```

## Configuration
The plugin offers several configuration options to customize its behavior.

### Mouse Settings
* **Movement Strategy**: Determines how the mouse moves. Options include `BEZIER` (default), `LINEAR`, etc.
* **Replay Library**: If `REPLAY` strategy is chosen, this defines which library to load.
* **Linear Steps**: If `LINEAR` strategy is chosen, this sets the number of steps (points) along the path (5-3000).

### Tree Settings
* **Tree Name**: Configures the script to search for trees of this type to chop (e.g., "Willow", "Oak").

### Overlays
* **Highlight Target Tree**: Visualizes which tree the script is currently targeting.
* **Render Path**: Draws the path the player is currently walking on the screen.
