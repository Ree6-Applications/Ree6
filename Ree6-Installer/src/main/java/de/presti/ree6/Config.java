package de.presti.ree6;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.util.Map;

/**
 * Config.
 */
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
            yamlFile.addDefault("config.version", "2.2.0");
            yamlFile.addDefault("config.creation", System.currentTimeMillis());
            yamlFile.addDefault("hikari.sql.user", "root");
            yamlFile.addDefault("hikari.sql.db", "root");
            yamlFile.addDefault("hikari.sql.pw", "yourpw");
            yamlFile.addDefault("hikari.sql.host", "localhost");
            yamlFile.addDefault("hikari.sql.port", 3306);
            yamlFile.addDefault("hikari.misc.storage", "sqlite");
            yamlFile.addDefault("hikari.misc.storageFile", "storage/Ree6.db");
            yamlFile.addDefault("hikari.misc.poolSize", 10);
            yamlFile.addDefault("dagpi.apitoken", "DAGPI.xyz API-Token");
            yamlFile.addDefault("amari.apitoken", "Amari API-Token");
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
            yamlFile.addDefault("bot.tokens.release", "ReleaseTokenhere");
            yamlFile.addDefault("bot.tokens.beta", "BetaTokenhere");
            yamlFile.addDefault("bot.tokens.dev", "DevTokenhere");

            try {
                yamlFile.save(getFile());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            try {
                yamlFile.load();
                migrateOldConfig();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Migrate configs to newer versions
     */
    public void migrateOldConfig() {
        String configVersion = yamlFile.getString("config.version");

        Map<String, Object> resources = yamlFile.getValues(true);
        if (configVersion == null) {
            // Migrating 1.10.0
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
                    exception.printStackTrace();
                }
            }
        } else {
            // Migrate from 2.0.0
            if (getFile().delete()) {
                init();

                for (Map.Entry<String, Object> entry : resources.entrySet()) {
                    if (compareVersion("2.2.0", configVersion)) {
                        String key = entry.getKey();

                        if (key.startsWith("youtube"))
                            continue;

                        yamlFile.set(key, entry.getValue());
                    }
                }

                try {
                    yamlFile.save(getFile());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Compare two version that are based on the x.y.z format.
     * @param versionA the base version.
     * @param versionB the version that should be tested against versionA.
     * @return True if versionA is above versionB.
     */
    public boolean compareVersion(String versionA, String versionB) {
        if (versionA == null) return false;
        if (versionB == null) return true;

        String[] split = versionA.split("\\.");

        int mayor = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        int patch = Integer.parseInt(split[2]);

        String[] split2 = versionB.split("\\.");
        int otherMayor = Integer.parseInt(split2[0]);
        int otherMinor = Integer.parseInt(split2[1]);
        int otherPatch = Integer.parseInt(split2[2]);

        if (mayor > otherMayor) return true;
        if (mayor == otherMayor && minor > otherMinor) return true;
        return mayor == otherMayor && minor == otherMinor && patch > otherPatch;
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
