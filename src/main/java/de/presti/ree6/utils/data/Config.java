package de.presti.ree6.utils.data;

import de.presti.ree6.bot.BotWorker;
import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.MemorySection;
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
            Path storageTemp = Path.of("storage/tmp");

            if (!Files.exists(storage))
                Files.createDirectory(storage);

            if (!Files.exists(storageTemp))
                Files.createDirectory(storageTemp);
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
            yamlFile.path("config")
                    .comment("Do not change this!")
                    .path("version").addDefault("2.4.11")
                    .parent().path("creation").addDefault(System.currentTimeMillis());

            yamlFile.path("hikari")
                    .comment("HikariCP Configuration").blankLine()
                    .path("sql").comment("SQL Configuration").blankLine()
                    .path("user").addDefault("root")
                    .parent().path("db").addDefault("root")
                    .parent().path("pw").addDefault("yourpw")
                    .parent().path("host").addDefault("localhost")
                    .parent().path("port").addDefault(3306)
                    .parent().parent().path("misc").comment("Misc Configuration").blankLine()
                    .path("storage").addDefault("sqlite").commentSide("Either use sqlite or mariadb.")
                    .parent().path("storageFile").addDefault("storage/Ree6.db")
                    .parent().path("poolSize").addDefault(10);

            yamlFile.path("heartbeat")
                    .comment("Heartbeat Configuration, for status reporting").blankLine()
                    .path("url").addDefault("none").commentSide("The URL to the Heartbeat-Server")
                    .parent().path("interval").addDefault(60);

            yamlFile.path("dagpi").path("apitoken").commentSide("Your Dagpi.xyz API-Token, for tweet image generation!")
                    .addDefault("DAGPI.xyz API-Token");

            yamlFile.setBlankLine("dagpi");

            yamlFile.path("amari").path("apitoken").commentSide("Your Amari API-Token, for Amari Level imports!")
                    .addDefault("Amari API-Token");

            yamlFile.setBlankLine("amari");

            yamlFile.path("sentry").path("dsn").commentSide("Your Sentry DSN, for error reporting!")
                    .addDefault("yourSentryDSNHere");

            yamlFile.setBlankLine("sentry");

            yamlFile.path("spotify")
                    .comment("Spotify Application Configuration, used to parse Spotify Tracks/Playlists to YouTube search queries.").blankLine()
                    .path("client").path("id").addDefault("yourspotifyclientid")
                    .parent().path("secret").addDefault("yourspotifyclientsecret");

            yamlFile.path("twitch")
                    .comment("Twitch Application Configuration, used for the StreamTools and Twitch Notifications.").blankLine()
                    .path("client").path("id").addDefault("yourtwitchclientidhere")
                    .parent().path("secret").addDefault("yourtwitchclientsecrethere");

            yamlFile.path("twitter")
                    .comment("Twitter Application Configuration, used for the Twitter Notifications.").blankLine()
                    .path("bearer").addDefault("yourTwitterBearerToken");

            yamlFile.path("reddit")
                    .comment("Reddit Application Configuration, used for the Reddit Notification.").blankLine()
                    .path("client").path("id").addDefault("yourredditclientid")
                    .parent().path("secret").addDefault("yourredditclientsecret");

            yamlFile.path("instagram")
                    .comment("Instagram Application Configuration, used for the Instagram Notification.").blankLine()
                    .path("username").addDefault("yourInstagramUsername")
                    .parent().path("password").addDefault("yourInstagramPassword");

            yamlFile.path("bot")
                    .comment("Discord Application Configuration, used for OAuth and Bot Authentication.").blankLine()
                    .path("tokens").path("release").addDefault("ReleaseTokenhere").commentSide("Token used when set to release build.")
                    .parent().path("beta").addDefault("BetaTokenhere").commentSide("Token used when set to beta build.")
                    .parent().path("dev").addDefault("DevTokenhere").commentSide("Token used when set to dev build.");

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
                log.error("Could not load config!", exception);
            }
        }
    }

    /**
     * Migrate configs to newer versions
     */
    public void migrateOldConfig() {
        String configVersion = yamlFile.getString("config.version", "1.9.0");

        if (compareVersion(configVersion, BotWorker.getBuild()) || configVersion.equals(BotWorker.getBuild()) ||
                configVersion.equals("2.4.3"))
            return;

        Map<String, Object> resources = yamlFile.getValues(true);

        // Migrate configs
        try {
            Files.copy(getFile().toPath(), new File("config-old.yml").toPath());
        } catch (Exception ignore) {
            log.warn("Could not move the old configuration file to config-old.yml!");
            log.warn("This means the config file is not backed up by us!");
        }

        if (getFile().delete()) {
            init();

            for (Map.Entry<String, Object> entry : resources.entrySet()) {
                String key = entry.getKey();

                boolean modified = false;

                if (key.startsWith("config"))
                    continue;

                if (entry.getValue() instanceof MemorySection)
                    continue;

                // Migrate to 1.10.0
                if (compareVersion("1.10.0", configVersion)) {

                    if (key.startsWith("mysql"))
                        key = key.replace("mysql", "hikari.sql");

                    if (key.endsWith(".rel"))
                        key = key.replace(".rel", ".release");

                    yamlFile.set(key, entry.getValue());
                    modified = true;
                }

                // Migrate to 2.2.0
                if (compareVersion("2.2.0", configVersion)) {

                    if (key.startsWith("youtube"))
                        continue;

                    yamlFile.set(key, entry.getValue());
                    modified = true;
                }


                // Migrate to 2.4.10
                if (compareVersion("2.4.10", configVersion)) {
                    if (key.startsWith("twitter") && !key.endsWith("bearer")) continue;
                }

                if (!modified) {
                    yamlFile.set(key, entry.getValue());
                }
            }

            if (compareVersion("2.2.0", configVersion)) {
                yamlFile.remove("youtube");
            }

            try {
                yamlFile.save(getFile());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Compare two version that are based on the x.y.z format.
     *
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
     *
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
     *
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
     *
     * @return The Configuration File as {@link File}.
     */
    public File getFile() {
        return new File("config.yml");
    }

}
