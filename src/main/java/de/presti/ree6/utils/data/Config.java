package de.presti.ree6.utils.data;

import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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

        try {
            Path storage = Path.of("storage");
            if (!Files.exists(storage))
                Files.createDirectory(storage);
        } catch (Exception exception) {
            log.error("Could not create Storage folder!", exception);
        }

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
            yamlFile.addDefault("config.version", "2.0.0");
            yamlFile.addDefault("config.creation", System.currentTimeMillis());
            yamlFile.addDefault("hikari.sql.user", "root");
            yamlFile.addDefault("hikari.sql.db", "root");
            yamlFile.addDefault("hikari.sql.pw", "yourpw");
            yamlFile.addDefault("hikari.sql.host", "localhost");
            yamlFile.addDefault("hikari.sql.port", 3306);
            yamlFile.addDefault("hikari.misc.storage", "sqlite");
            yamlFile.addDefault("hikari.misc.storageFile", "storage/Ree6.db");
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
            yamlFile.addDefault("youtube.api.key2", "youryoutubeapikey2");
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
                migrateOldConfig();
            } catch (Exception exception) {
                log.error("Could not load config!",exception);
            }
        }
    }

    /**
     * Migrate from 1.10.0 config to 2.0.0 config.
     */
    public void migrateOldConfig() {
        if (yamlFile.getString("config.version") == null) {
            Map<String, Object> resources = yamlFile.getValues(true);
            if (getFile().delete()) {
                init();

                for (Map.Entry<String, Object> entry : resources.entrySet()) {
                    String key = entry.getKey();

                    if (key.startsWith("mysql"))
                        key = key.replace("mysql", "hikari.sql");

                    if (key.endsWith(".rel"))
                        key = key.replace(".rel", ".release");

                    yamlFile.set(key, entry.getValue());
                }

                try {
                    yamlFile.save(getFile());
                } catch (Exception exception) {
                    log.error("Could not save config file!", exception);
                }
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
