package de.presti.ree6.utils;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    FileConfiguration cfg;

    public void init() {

            cfg = getConfig();

        if (!getFile().exists()) {
            cfg.options().copyDefaults(true);
            cfg.options().copyHeader(true);
            cfg.options().header("################################\n" +
                    "#                              #\n" +
                    "# Ree6 Config File             #\n" +
                    "# by Presti                    #\n" +
                    "#                              #\n" +
                    "################################\n");
            cfg.addDefault("mysql.user", "root");
            cfg.addDefault("mysql.db", "root");
            cfg.addDefault("mysql.pw", "yourpw");
            cfg.addDefault("mysql.host", "localhost");
            cfg.addDefault("mysql.port", 3306);
            cfg.addDefault("giphy.apitoken", "yourgiphytokenherepog");
            cfg.addDefault("dagpi.apitoken", "yourdagpixyztokenhere");
            cfg.addDefault("spotify.client.id", "yourspotifyclientid");
            cfg.addDefault("spotify.client.secret", "yourspotifyclientsecret");
            cfg.addDefault("twitch.client.id", "yourtwitchclientidhere");
            cfg.addDefault("twitch.client.secret", "yourtwitchclientsecrethere");
            cfg.addDefault("youtube.api.key", "youryoutubeapikey");
            cfg.addDefault("bot.tokens.rel", "ReleaseTokenhere");
            cfg.addDefault("bot.tokens.dev", "DevTokenhere");

            try {
                cfg.save(getFile());
            } catch (Exception ignored) {
            }

        }
    }

    public FileConfiguration getConfig() {
       return YamlConfiguration.loadConfiguration(getFile());
    }

    public File getFile() {
        return new File("config.yml");
    }

}
