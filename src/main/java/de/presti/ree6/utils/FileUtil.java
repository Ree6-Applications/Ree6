package de.presti.ree6.utils;

import de.presti.ree6.bot.*;
import de.presti.ree6.main.Main;

public class FileUtil {

    public static String getToken() {
        if (BotInfo.version == BotVersion.DEV) {
            return Main.config.getConfig().getString("bot.tokens.dev");
        } else if (BotInfo.version == BotVersion.PUBLIC || BotInfo.version == BotVersion.PRERELASE) {
            return Main.config.getConfig().getString("bot.tokens.rel");
        } else {
            return "error";
        }
    }

}