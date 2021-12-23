package de.presti.ree6.utils;

import de.presti.ree6.main.Main;

public class LoggerImpl {

    public static void log(String name, String message) {
        Main.instance.getLogger().info("[{0}] {1}", name, message);
    }

}
