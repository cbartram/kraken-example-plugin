# Jewelry Plugin

The Jewelry Plugin is an example automation script built using the Kraken API. 
It automates the process of crafting sapphire necklaces at the Edgeville furnace, demonstrating interaction with game objects, banking, and interface management.

## Features
- Automates crafting sapphire necklaces at Edgeville.
- Banks at Edgeville Bank when materials are depleted.
- Withdraws Gold Bars and Sapphires automatically.
- Handles pathfinding and walking between the bank and the furnace.

## Requirements & Usage
To use this plugin effectively, please ensure the following:

1. **Mould**: You must have a **Necklace mould** in your inventory.
2. **Crafting Level**: You need a Crafting level of at least **22** to craft sapphire necklaces.
3. **Starting Location**: Your character must be located at **Edgeville Bank**.
4. **Materials**: Ensure you have plenty of **Gold bars** and **Sapphires** in your bank.

## Limitations
- **Location**: This plugin is specifically designed for **Edgeville** (Edgeville Bank and Furnace). It does not support other locations.
- **Item**: Currently hardcoded to craft **Sapphire Necklaces**.

## Running the Plugin
To run the jewelry plugin, use the following commands in your terminal:

```shell
./gradlew build

java -jar ./build/libs/kraken-example-plugin-1.0.0.jar --developer-mode com.krakenplugins.example.jewelry.JewelryPlugin
```

## Configuration
The plugin offers several configuration options to customize its behavior.

### Mouse Settings
* **Use Mouse Movement**: When true, the mouse will be moved on the canvas.
* **Movement Strategy**: Determines how the mouse moves. Options include `BEZIER` (default), `LINEAR`, etc.
* **Replay Library**: If `REPLAY` strategy is chosen, this defines which library to load.
* **Linear Steps**: If `LINEAR` strategy is chosen, this sets the number of steps (points) along the path (5-3000).

### Overlays
* **Debug**: Show debug information and overlays.
