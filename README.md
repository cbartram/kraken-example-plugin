<br />
<div align="center">
  <a href="https://kraken-plugins.com">
    <img src="lib/src/main/resources/kraken.png" alt="Logo" width="128" height="128">
  </a>

<h3 align="center">Kraken Example Plugins</h3>

  <p align="center">
   An set of example automation plugins utilizing the Kraken API
    <br />
</div>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]

# Kraken Example Plugins

This repository contains examples for writing automation plugins using the [Kraken API](https://github.com/cbartram/kraken-api.git).
The Kraken API extends the RuneLite API with the ability to interact with various game entities, including:

- Widgets (Prayers, Spells, Interfaces, etc...)
- NPC's
- Players
- Ground Items
- Game Objects
- Equipment
- World Hopping
- Container Items (Inventory, Bank, etc...)
- and more!

The Kraken API also ships with several handy features for developing automation plugins (scripts) right on top of RuneLite
like `TaskChain`'s, pathfinding, advanced mouse movement, abstractions for writing scripts, and network packet classes. 
This repository contains several examples of fully functioning automation scripts showcasing the API's capabilities.

### Plugin & Script Requirements

You can read more about the individual plugins, their features, and their requirements
in their respective README's linked below.

- [Mining Plugin](docs/MINING.md)
- [Woodcutting Plugin](docs/WOODCUTTING.md)

# QuickStart

To set up your development environment we recommend following [this guide on RuneLite's Wiki](https://github.com/runelite/runelite/wiki/Building-with-IntelliJ-IDEA).
You must add `-ea` to your VM args to enable assertions and add `--developer-mode` and `com.krakenplugins.example.<package>.<class>`
as arguments when you run the JAR. See example below.

Once you have the example plugin cloned and setup within Intellij run plugins with:

```shell
export GITHUB_ACTOR=<github-username>
export GITHUB_TOKEN=<github-personal-access-token>

./gradlew build

java -jar ./build/libs/kraken-example-plugin-1.0.0.jar --developer-mode com.krakenplugins.example.mining.MiningPlugin
```

You should see: "Mining Plugin" within your set of plugins in the sidebar.

> :warning: Note: Pass the full package and class name of the plugin you want to run as the second argument
> when starting the plugin. For example use `com.krakenplugins.example.woodcutting.WoodcuttingPlugin` to start the 
> Woodcutting plugin instead of the mining plugin.

## Gradle Kraken API

Please see [these docs](https://github.com/cbartram/kraken-api?tab=readme-ov-file#gradle-example-recommended) for including the Kraken API
as part of your RuneLite plugin's build process.

## 🛠 Built With

* [Java](https://www.java.org/) — Core language
* [Gradle](https://gradle.org/) — Build tool
* [RuneLite](https://runelite.net) — Used for as the backbone for the API
* [Kraken API](https://github.com/cbartram/kraken-api) - Interaction API

---

## 🤝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

---

## 🔖 Versioning

We use [Semantic Versioning](http://semver.org/).
See the [tags on this repository](https://github.com/cbartram/kraken-api/tags) for available releases.

---

## 📜 License

This project is licensed under the [GNU General Public License 3.0](LICENSE.md).

---

## 🙏 Acknowledgments

* **RuneLite** — The splash screen and much of the core codebase come from RuneLite.
* **Microbot** — For clever ideas on client and plugin interaction.
* **Packet Utils** - Plugin from Ethan Vann providing access to complex packet sending functionality which was used to develop the core.packet package of the API


[contributors-shield]: https://img.shields.io/github/contributors/cbartram/kraken-example-plugin.svg?style=for-the-badge
[contributors-url]: https://github.com/cbartram/kraken-example-plugin/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/cbartram/kraken-example-plugin.svg?style=for-the-badge
[forks-url]: https://github.com/cbartram/kraken-example-plugin/network/members
[stars-shield]: https://img.shields.io/github/stars/cbartram/kraken-example-plugin.svg?style=for-the-badge
[stars-url]: https://github.com/cbartram/kraken-example-plugin/stargazers
[issues-shield]: https://img.shields.io/github/issues/cbartram/kraken-example-plugin.svg?style=for-the-badge
[issues-url]: https://github.com/cbartram/kraken-example-plugin/issues