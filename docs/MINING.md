# Mining Plugin

The Mining Plugin is an example automation script built using the Kraken API. 
It demonstrates how to create a script that navigates between a bank and a mining location, interacts with game objects
(iron rocks and bank booths), and manages inventory.

## Features
- Automates mining Iron ore at Varrock East Mine.
- Banks ores at Varrock East Bank when inventory is full.
- Handles pathfinding and walking between the bank and the mine.
- Manages run energy.

## Requirements & Usage
To use this plugin effectively, please ensure the following:

1. **Pickaxe**: You must have a pickaxe equipped or in your inventory that matches your mining level.
2. **Starting Location**: Your character must be located at **Varrock East Bank**.
3. **Inventory**: Start with an **empty inventory** (except for your pickaxe if it is not equipped).

## Limitations
- **Ore Type**: Currently, this plugin only supports mining **Iron**.
- **Location**: It is hardcoded for **Varrock East Mine** and **Varrock East Bank**.

## Running the Plugin
To run the mining plugin, use the following commands in your terminal:

```shell
./gradlew build

java -jar ./build/libs/kraken-example-plugin-1.0.0.jar --developer-mode com.krakenplugins.example.mining.MiningPlugin
```

## Configuration
The plugin offers several configuration options to customize its behavior.

### Mouse Settings
* **Movement Strategy**: Determines how the mouse moves. Options include `BEZIER` (default), `LINEAR`, etc.
* **Replay Library**: If `REPLAY` strategy is chosen, this defines which library to load.
* **Linear Steps**: If `LINEAR` strategy is chosen, this sets the number of steps (points) along the path (5-3000).

### Run Energy
* **Run Energy Min**: The minimum energy percentage to toggle run.
* **Run Energy Max**: The maximum energy percentage to toggle run.
The script will enable run at a random point between these two values.

### Overlays
* **Highlight Target Rock**: Visualizes which rock the script is currently targeting.
* **Render Path**: Draws the path the player is currently walking on the screen.
