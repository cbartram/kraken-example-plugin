# Firemaking Plugin

The Firemaking Plugin is an example automation script built using the Kraken API. It automates the process of burning logs to level up the Firemaking skill, specifically focusing on the usage of bonfires for efficiency.

## Features
- Automates firemaking at the Grand Exchange.
- Prioritizes using **bonfires** over creating lines of fires.
- Detects existing bonfires to add logs to; if none are available, it creates a new fire and converts it into a bonfire.
- Supports various log types via configuration.
- Includes overlays for status and debugging.

## Requirements & Usage
To use this plugin effectively, please ensure the following:

1. **Tinderbox**: You must have a tinderbox in your inventory.
2. **Logs**: Ensure you have a supply of the desired logs in your bank.
3. **Starting Location**: Your character must be located at the **Grand Exchange**.

## Limitations
- **Location**: This plugin is specifically designed for the **Grand Exchange**. It does not support other banking or firemaking locations at this time.
- **Method**: The script focuses on bonfires and does not support the traditional method of lighting lines of fires.

## Running the Plugin
To run the firemaking plugin, use the following commands in your terminal:

```shell
./gradlew build

java -jar ./build/libs/kraken-example-plugin-1.0.0.jar --developer-mode com.krakenplugins.example.firemaking.FiremakingPlugin
```

## Configuration
The plugin offers several configuration options to customize its behavior.

### Bank Settings
* **Bank Pin**: If your account has a bank pin, you can enter it here (4 digits) to have the script automatically enter it for you.

### Mouse Settings
* **Use Mouse Movement**: Toggles whether the mouse cursor physically moves on the canvas.
* **Movement Strategy**: Determines how the mouse moves. Options include `BEZIER` (default), `LINEAR`, etc.
* **Replay Library**: If `REPLAY` strategy is chosen, this defines which library to load.
* **Linear Steps**: If `LINEAR` strategy is chosen, this sets the number of steps (points) along the path (5-3000).

### Log Settings
* **Log Name**: Configures the name of the logs the script should withdraw and burn (e.g., "Logs", "Oak logs", "Willow logs").

### Overlays
* **Show Target Banker**: Highlights the banker NPC the script is currently interacting with.
* **Show Target Fire**: Highlights the fire object the script is targeting for bonfire use.
* **Debug**: Enables additional debug information, such as script area checks and other technical overlays.
