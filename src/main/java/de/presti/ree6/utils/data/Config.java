package de.presti.ree6.utils.data;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;

public class Config {

    private YamlFile yamlFile;

    public void init() {

        yamlFile = createConfiguration();

        if (!getFile().exists()) {
            yamlFile.options().copyHeader();
            yamlFile.options().copyDefaults();
            yamlFile.options().header("""
                    ################################
                    #                              #
                    # Ree6 Config File             #
                    # by Presti                    #
                    #                              #
                    ################################
                    """);
            yamlFile.addDefault("mysql.user", "root");
            yamlFile.addDefault("mysql.db", "root");
            yamlFile.addDefault("mysql.pw", "yourpw");
            yamlFile.addDefault("mysql.host", "localhost");
            yamlFile.addDefault("mysql.port", 3306);
            yamlFile.addDefault("giphy.apitoken", "yourgiphytokenherepog");
            yamlFile.addDefault("dagpi.apitoken", "yourdagpixyztokenhere");
            yamlFile.addDefault("raygun.apitoken", "yourrayguntokenherepog");
            yamlFile.addDefault("spotify.client.id", "yourspotifyclientid");
            yamlFile.addDefault("spotify.client.secret", "yourspotifyclientsecret");
            yamlFile.addDefault("twitch.client.id", "yourtwitchclientidhere");
            yamlFile.addDefault("twitch.client.secret", "yourtwitchclientsecrethere");
            yamlFile.addDefault("twitter.consumer.key", "yourTwitterConsumerKey");
            yamlFile.addDefault("twitter.consumer.secret", "yourTwitterConsumerSecret");
            yamlFile.addDefault("twitter.access.key", "yourTwitterAccessKey");
            yamlFile.addDefault("twitter.access.secret", "yourTwitterAccessSecret");
            yamlFile.addDefault("youtube.api.key", "youryoutubeapikey");
            yamlFile.addDefault("bot.tokens.rel", "ReleaseTokenhere");
            yamlFile.addDefault("bot.tokens.dev", "DevTokenhere");

            try {
                yamlFile.save(getFile());
            } catch (Exception ignored) {
            }
        } else {
            try {
                yamlFile.load();
            } catch (Exception ignored) {
            }
        }
    }

    public YamlFile createConfiguration() {
        try {
            return new YamlFile(getFile());
        } catch (Exception e) {
            return new YamlFile();
        }
    }

    public YamlFile getConfiguration() {
        return yamlFile;
    }

    public File getFile() {
        return new File("config.yml");
    }

}
