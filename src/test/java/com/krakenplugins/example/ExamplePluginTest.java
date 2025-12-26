package com.krakenplugins.example;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.Plugin;

public class ExamplePluginTest {
    public static void main(String[] args) throws Exception {
        String targetPlugin = args[0];

        for (String arg : args) {
            if (arg.contains("com.krakenplugins.example")) {
                targetPlugin = arg;
                break;
            }
        }

        System.out.println("Attempting to load: " + targetPlugin);

        Class<? extends Plugin> clazz = (Class<? extends Plugin>) Class.forName(targetPlugin);
        ExternalPluginManager.loadBuiltin(clazz);
        RuneLite.main(args);
    }
}