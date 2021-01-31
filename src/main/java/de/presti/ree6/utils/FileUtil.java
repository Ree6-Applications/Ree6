package de.presti.ree6.utils;

import de.presti.ree6.bot.*;
import de.presti.ree6.main.Main;

import java.io.*;

public class FileUtil {

    public static String getToken() {
        if (BotInfo.version == BotVersion.DEV) {
            return Main.config.getConfig().getString("bot.tokens.dev");
        } else if (BotInfo.version == BotVersion.PUBLIC) {
            return Main.config.getConfig().getString("bot.tokens.rel");
        } else {
            return "Du Dumm?";
        }
    }

}