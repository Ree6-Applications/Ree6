package de.presti.ree6.utils.data;

import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;

/**
 * Config.
 */
@Slf4j
public class Config {

    /**
     * The Configuration.
     */
    private YamlFile yamlFile;

    /**
     * Initialize the Configuration.
     */
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
            yamlFile.addDefault("hikari.sql.user", "root");
            yamlFile.addDefault("hikari.sql.db", "root");
            yamlFile.addDefault("hikari.sql.pw", "yourpw");
            yamlFile.addDefault("hikari.sql.host", "localhost");
            yamlFile.addDefault("hikari.sql.port", 3306);
            yamlFile.addDefault("hikari.misc.storage", "sqllite");
            yamlFile.addDefault("hikari.misc.poolSize", 10);
            yamlFile.addDefault("dagpi.apitoken", "yourdagpixyztokenhere");
            yamlFile.addDefault("sentry.dsn", "yourSentryDSNHere");
            yamlFile.addDefault("spotify.client.id", "yourspotifyclientid");
            yamlFile.addDefault("spotify.client.secret", "yourspotifyclientsecret");
            yamlFile.addDefault("twitch.client.id", "yourtwitchclientidhere");
            yamlFile.addDefault("twitch.client.secret", "yourtwitchclientsecrethere");
            yamlFile.addDefault("twitter.consumer.key", "yourTwitterConsumerKey");
            yamlFile.addDefault("twitter.consumer.secret", "yourTwitterConsumerSecret");
            yamlFile.addDefault("twitter.access.key", "yourTwitterAccessKey");
            yamlFile.addDefault("twitter.access.secret", "yourTwitterAccessSecret");
            yamlFile.addDefault("reddit.client.id", "yourredditclientid");
            yamlFile.addDefault("reddit.client.secret", "yourredditclientsecret");
            yamlFile.addDefault("instagram.username", "yourInstagramUsername");
            yamlFile.addDefault("instagram.password", "yourInstagramPassword");
            yamlFile.addDefault("youtube.api.key", "youryoutubeapikey");
            yamlFile.addDefault("bot.tokens.release", "ReleaseTokenhere");
            yamlFile.addDefault("bot.tokens.beta", "BetaTokenhere");
            yamlFile.addDefault("bot.tokens.dev", "DevTokenhere");

            try {
                yamlFile.save(getFile());
            } catch (Exception exception) {
                log.error("Could not save config file!", exception);
            }
        } else {
            try {
                yamlFile.load();
            } catch (Exception exception) {
                log.error("Could not load config!",exception);
            }
        }
    }

    /**
     * Create a new Configuration.
     * @return The Configuration as {@link YamlFile}.
     */
    public YamlFile createConfiguration() {
        try {
            return new YamlFile(getFile());
        } catch (Exception e) {
            return new YamlFile();
        }
    }

    /**
     * Get the Configuration.
     * @return The Configuration as {@link YamlFile}.
     */
    public YamlFile getConfiguration() {
        if (yamlFile == null) {
            init();
        }

        return yamlFile;
    }

    /**
     * Get the Configuration File.
     * @return The Configuration File as {@link File}.
     */
    public File getFile() {
        return new File("config.yml");
    }

}
