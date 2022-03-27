package de.presti.ree6.utils.storage;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    public FileConfiguration cfg;

    public void init() {

        cfg = getConfig();

        if (!getFile().exists()) {
            cfg.options().copyDefaults(true);
            cfg.options().copyHeader(true);
            cfg.options().header("""
                    ################################
                    #                              #
                    # Ree6 Config File             #
                    # by Presti                    #
                    #                              #
                    ################################
                    """);
            cfg.addDefault("mysql.user", "root");
            cfg.addDefault("mysql.db", "root");
            cfg.addDefault("mysql.pw", "yourpw");
            cfg.addDefault("mysql.host", "localhost");
            cfg.addDefault("mysql.port", 3306);
            cfg.addDefault("giphy.apitoken", "yourgiphytokenherepog");
            cfg.addDefault("dagpi.apitoken", "yourdagpixyztokenhere");
            cfg.addDefault("raygun.apitoken", "yourrayguntokenherepog");
            cfg.addDefault("spotify.client.id", "yourspotifyclientid");
            cfg.addDefault("spotify.client.secret", "yourspotifyclientsecret");
            cfg.addDefault("twitch.client.id", "yourtwitchclientidhere");
            cfg.addDefault("twitch.client.secret", "yourtwitchclientsecrethere");
            cfg.addDefault("twitter.consumer.key", "yourTwitterConsumerKey");
            cfg.addDefault("twitter.consumer.secret", "yourTwitterConsumerSecret");
            cfg.addDefault("twitter.access.key", "yourTwitterAccessKey");
            cfg.addDefault("twitter.access.secret", "yourTwitterAccessSecret");
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
