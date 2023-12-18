package de.presti.ree6.utils.data;

import de.presti.ree6.bot.BotWorker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.MemorySection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
     * The YamlFile for the temp file.
     */
    private YamlFile yamlTempFile;

    /**
     * The config version.
     */
    private final String version = "3.0.19";

    /**
     * Initialize the Configuration.
     */
    public void init() {
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

        createTemporalFile();
        createConfigFile();
    }

    /**
     * Create a new Temporal File.
     */
    public void createTemporalFile() {
        yamlTempFile = createTemporal();

        if (!getTemporalFile().exists()) {
            yamlTempFile.options().copyHeader();
            yamlTempFile.options().copyDefaults();
            yamlTempFile.options().header("""
                    ################################
                    #                              #
                    # Ree6 Temporal Info           #
                    # by Presti                    #
                    #                              #
                    ################################
                    """);
        }
    }

    /**
     * Create a new Config File.
     */
    public void createConfigFile() {
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
            yamlFile.path("config")
                    .comment("Do not change this!")
                    .path("version").addDefault(version)
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
                    .path("storage").addDefault("sqlite").commentSide("Possible entries: sqlite, mariadb, postgresql, h2, h2-server")
                    .parent().path("storageFile").addDefault("storage/Ree6.db")
                    .parent().path("createEmbeddedServer").addDefault(false).commentSide("Should an instance of an embedded Server be created? Only used for H2-Server.")
                    .parent().path("poolSize").addDefault(10);

            yamlFile.path("bot")
                    .comment("Discord Application and overall Bot Configuration, used for OAuth, Bot Authentication, and customization.").blankLine()
                    .path("tokens").path("release").addDefault("ReleaseTokenhere").commentSide("Token used when set to release build.")
                    .parent().path("beta").addDefault("BetaTokenhere").commentSide("Token used when set to beta build.")
                    .parent().path("dev").addDefault("DevTokenhere").commentSide("Token used when set to dev build.")
                    .parent().parent().path("misc").comment("Configuration for the Bot itself.").blankLine()
                    .path("status").addDefault("ree6.de | %guilds% Servers. (%shard%)").commentSide("The Status of the Bot.")
                    .parent().path("feedbackChannelId").addDefault(0L).commentSide("The Channel used for Feedback.")
                    .parent().path("ownerId").addDefault(321580743488831490L).commentSide("The ID of the Bot Owner. Change this to yours!")
                    .parent().path("predefineInformation").addDefault("""
                            You are Ree6 a Discord bot.
                            """).commentSide("Predefined Information for the AI.")
                    .parent().path("invite").addDefault("https://invite.ree6.de").commentSide("The Invite Link of the Bot. (Can not be empty)")
                    .parent().path("support").addDefault("https://support.ree6.de").commentSide("The Support Server Link of the Bot. (Can not be empty)")
                    .parent().path("github").addDefault("https://github.ree6.de").commentSide("The GitHub Link of the Bot. (Can not be empty)")
                    .parent().path("website").addDefault("https://ree6.de").commentSide("The Website Link of the Bot. (Can not be empty)")
                    .parent().path("webinterface").addDefault("https://cp.ree6.de").commentSide("The Webinterface Link of the Bot. (Can not be empty)")
                    .parent().path("recording").addDefault("https://cp.ree6.de/external/recording").commentSide("The Recording Link of the Bot.")
                    .parent().path("twitchAuth").addDefault("https://cp.ree6.de/external/twitch").commentSide("The Twitch Authentication Link of the Bot.")
                    .parent().path("advertisement").addDefault("powered by Tube-hosting").commentSide("The Advertisement in Embed Footers and the rest.")
                    .parent().path("name").addDefault("Ree6").commentSide("The Name of the Bot.")
                    .parent().path("shards").addDefault(1).commentSide("The shard amount of the Bot. Check out https://anidiots.guide/understanding/sharding/#sharding for more information.")
                    .parent().path("defaultLanguage").addDefault("en-GB").commentSide("The default Language of the Bot. Based on https://discord.com/developers/docs/reference#locales")
                    .parent().path("allowRecordingInChat").addDefault(false).commentSide("If you want to allow users to let the Bot send their recording into the chat.")
                    .parent().path("hideModuleNotification").addDefault(false).commentSide("Should the Notification for disabled Modules be hidden?")
                    .parent().path("debug").addDefault(false).commentSide("Should the Bot be in Debug Mode? This will enable more logging.")
                    .parent().path("defaultPrefix").addDefault("ree!").commentSide("The default Prefix of the Bot.")
                    .parent().path("textFont").addDefault("Verdana").commentSide("The Font that is being used in Images for the Text.")
                    .parent().path("leveling").comment("Customize the leveling module in Ree6.").blankLine()
                    .path("resets").comment("""
                            When should Ree6 stop the current progress of the user?
                            This means if someone mutes themselves, for example, they receive all the XP they would have gotten when they left that instant.
                            And when they unmute, again the XP gather restarts from 0. So they don't lose their progress, but don't get XP from being mute.""").blankLine()
                    .path("mute").addDefault(true).commentSide("Should an XP reset be triggered when a user mutes themselves?")
                    .parent().path("muteGlobal").addDefault(true).commentSide("Should an XP reset be triggered when a user gets muted on the Server?")
                    .parent().path("deafen").addDefault(true).commentSide("Should an XP reset be triggered when a user deafens themselves?")
                    .parent().path("deafenGlobal").addDefault(true).commentSide("Should an XP reset be triggered when a user gets deafened on the Server?")
                    .parent()
                    .parent().parent().path("modules").comment("Customize the active modules in Ree6.").blankLine()
                    .path("moderation").addDefault(true).commentSide("Enable the moderation module.")
                    .parent().path("music").addDefault(true).commentSide("Enable the music module.")
                    .parent().path("fun").addDefault(true).commentSide("Enable the fun commands.")
                    .parent().path("community").addDefault(true).commentSide("Enable the community commands.")
                    .parent().path("economy").addDefault(true).commentSide("Enable the economy commands.")
                    .parent().path("level").addDefault(true).commentSide("Enable the level module.")
                    .parent().path("nsfw").addDefault(true).commentSide("Enable the nsfw module.")
                    .parent().path("info").addDefault(true).commentSide("Enable the info commands.")
                    .parent().path("hidden").addDefault(true).commentSide("Enable the hidden commands.")
                    .parent().path("logging").addDefault(true).commentSide("Enable the logging module.")
                    .parent().path("notifier").addDefault(true).commentSide("Enable the notifier module.")
                    .parent().path("streamtools").addDefault(true).commentSide("Enable the Stream-tools module.")
                    .parent().path("temporalvoice").addDefault(true).commentSide("Enable the Temporal-voice module.")
                    .parent().path("tickets").addDefault(true).commentSide("Enable the Tickets module.")
                    .parent().path("suggestions").addDefault(true).commentSide("Enable the suggestions module.")
                    .parent().path("customcommands").addDefault(true).commentSide("Enable the custom Commands module.")
                    .parent().path("customevents").addDefault(true).commentSide("Enable the custom Events module.")
                    .parent().path("ai").addDefault(true).commentSide("Enable the AI module.")
                    .parent().path("addons").addDefault(false).commentSide("Enable the Addons module.")
                    .parent().path("news").addDefault(true).commentSide("Enable the news command/module.")
                    .parent().path("games").addDefault(true).commentSide("Enable the Games module.")
                    .parent().path("reactionroles").addDefault(true).commentSide("Enable the reaction-roles module.")
                    .parent().path("slashcommands").addDefault(true).commentSide("Enable the slash-commands support.")
                    .parent().path("messagecommands").addDefault(true).commentSide("Enable the message-commands support.");

            yamlFile.path("lavalink")
                    .comment("Lavalink Configuration, for lavalink support.").blankLine()
                    .path("enable").addDefault(false).commentSide("If you want to use Lavalink.")
                    .parent().path("nodes")
                    .addDefault(List.of(Map.of("name", "Node Name", "host", "node.mylava.link", "port", 0, "secure", false, "password", "none")))
                    .comment("Lavalink Nodes Configuration.").blankLine();

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

            yamlFile.path("openai").path("apiToken").commentSide("Your OpenAI API-Token, for ChatGPT!")
                    .addDefault("OpenAI API-Token")
                    .parent().path("apiUrl").addDefault("https://api.openai.com/v1/chat/completions").commentSide("The URL to the OpenAI API.")
                    .parent().path("model").addDefault("gpt-3.5-turbo-0301").commentSide("The Model used for the OpenAI API.");

            yamlFile.setBlankLine("openai");

            yamlFile.path("sentry")
                    .path("enable").commentSide("If you want to use Sentry.").addDefault(true)
                    .parent().path("dsn").commentSide("Your Sentry DSN, for error reporting!")
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
                configVersion.equals(version))
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


                // Migrate to 2.4.11
                if (compareVersion("2.4.11", configVersion)) {
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
     * Compare two versions that are based on the x.y.z format.
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

    /**
     * Create a new Temporal Info file.
     *
     * @return The Temporal Info file as {@link YamlFile}.
     */
    public YamlFile createTemporal() {
        try {
            return yamlTempFile = new YamlFile(getTemporalFile());
        } catch (Exception e) {
            return new YamlFile();
        }
    }

    /**
     * Get the Temporal Info file.
     *
     * @return The Temporal Info file as {@link YamlFile}.
     */
    public YamlFile getTemporal() {
        if (yamlTempFile == null)
            return createTemporal();

        return yamlTempFile;
    }

    /**
     * Get the Temporal Info file.
     *
     * @return The Temporal Info file as {@link File}.
     */
    public File getTemporalFile() {
        return new File("storage/temp.yml");
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LavaLinkNodeConfig {
        String name;
        String host;
        int port;
        boolean secure;
        String password;

        public String buildAddress() {
            String lavalinkUrl = getHost();

            if (isSecure()) {
                lavalinkUrl = "wss://" + lavalinkUrl;
            } else {
                lavalinkUrl = "ws://" + lavalinkUrl;
            }

            return lavalinkUrl + ":" + getPort();
        }
    }

}
