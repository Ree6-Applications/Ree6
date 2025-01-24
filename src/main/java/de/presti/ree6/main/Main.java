package de.presti.ree6.main;

import best.azura.eventbus.core.EventBus;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.pubsub.PubSubSubscription;
import com.google.gson.JsonObject;
import de.presti.ree6.addons.ReePluginManager;
import de.presti.ree6.audio.music.MusicWorker;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.commands.exceptions.CommandInitializerException;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.events.*;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.module.actions.streamtools.container.StreamActionContainerCreator;
import de.presti.ree6.module.game.core.GameManager;
import de.presti.ree6.module.game.impl.musicquiz.util.MusicQuizUtil;
import de.presti.ree6.module.giveaway.GiveawayManager;
import de.presti.ree6.module.invite.InviteContainerManager;
import de.presti.ree6.module.logger.LoggerQueue;
import de.presti.ree6.sql.DatabaseTyp;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Giveaway;
import de.presti.ree6.sql.entities.ScheduledMessage;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.entities.TwitchIntegration;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.util.SQLConfig;
import de.presti.ree6.sql.util.SettingsManager;
import de.presti.ree6.utils.apis.ChatGPTAPI;
import de.presti.ree6.utils.apis.Notifier;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import de.presti.ree6.utils.config.Config;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.OptionParser;
import de.presti.ree6.utils.external.RequestUtility;
import de.presti.ree6.utils.oauth.CustomOAuth2Credential;
import de.presti.ree6.utils.oauth.CustomOAuth2Util;
import de.presti.ree6.utils.others.ThreadUtil;
import io.sentry.Sentry;
import lavalink.client.io.jda.JdaLavalink;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Main Application class, used to store Instances of System Relevant classes.
 */
@Slf4j
@Getter
@Setter(AccessLevel.PRIVATE)
public class Main {
    /**
     * An Instance of the class itself.
     */

    static Main instance;

    /**
     * Instance of the Notifier Manager, used to manage the Notifier Tools.
     */
    Notifier notifier;

    /**
     * Instance of the EventBus, used to manage the Events.
     */
    EventBus eventBus;

    /**
     * Instance of the Command.
     */
    CommandManager commandManager;

    /**
     * Instance of the PluginManager, used to manage the Plugins.
     */
    PluginManager pluginManager;

    /**
     * Instance of the GiveawayManager, used to manage the Giveaways.
     */
    GiveawayManager giveawayManager;

    /**
     * Instance of the InviteContainerManager, used to manage all the Guild Invites for the Invite Logger.
     */
    InviteContainerManager inviteContainerManager;

    /**
     * Instance of the LoggerQueue, used to merge Logs to prevent Rate-Limits.
     */
    LoggerQueue loggerQueue;

    /**
     * Instance of the MusicWorker used to manage the Music-Player.
     */
    MusicWorker musicWorker;

    /**
     * Instance of the ChatGPT API used for making the setup process easier and give people a better experience.
     */
    ChatGPTAPI chatGPTAPI;

    /**
     * Instance of the Config System.
     */
    Config config;

    /**
     * Instance of the Lavalink.
     */
    JdaLavalink lavalink;

    /**
     * String used to identify the last day.
     */
    String lastDay = "";

    /**
     * Main methode called when Application starts.
     *
     * @param args Start Arguments.s
     */
    public static void main(String[] args) {

        OptionParser cliArguments = new OptionParser(args, true);

        // To allow Image creation on CPU.
        if (!cliArguments.isEnabled("noHeadless"))
            System.setProperty("java.awt.headless", "true");

        // Create the Main instance.
        instance = new Main();

        // Create an Instance of the EventBus.
        getInstance().setEventBus(new EventBus());

        // Create the LoggerQueue Instance.
        getInstance().setLoggerQueue(new LoggerQueue());

        // Create the Config System Instance.
        getInstance().setConfig(new Config(cliArguments.getValueOrDefault("config", "config.yml")));

        // Initialize the Config.
        getInstance().getConfig().init();

        ArrayUtil.temporalVoicechannel.addAll(getInstance().getConfig().getTemporal().getStringList("temporalvoice"));

        if (BotConfig.shouldUseSentry()) {
            log.info("Creating Sentry Instance.");

            // Create a Sentry Instance to send Exception to an external Service for bug fixing.
            Sentry.init(options -> {
                String dsn = getInstance().getConfig().getConfiguration().getString("sentry.dsn");
                options.setDsn((dsn == null || dsn.equalsIgnoreCase("yourSentryDSNHere")) ? "" : dsn);

                // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
                // We recommend adjusting this value in production.
                options.setTracesSampleRate(1.0);

                // When first trying Sentry, it's good to see what the SDK is doing:
                options.setRelease(BotWorker.getBuild());
            });

            Thread.setDefaultUncaughtExceptionHandler((t, e) -> Sentry.captureException(e));
        }

        log.info("Starting preparations of the Bot...");

        if (!cliArguments.isEnabled("skip-download")) {
            LanguageService.downloadLanguages();
            downloadMisc("storage");
        } else {
            LanguageService.initializeLanguages();
        }

        log.info("Finished preparations of the Bot!");

        log.info("Starting Ree6!");

        DatabaseTyp databaseTyp;

        switch (getInstance().getConfig().getConfiguration().getString("hikari.misc.storage").toLowerCase()) {
            case "mariadb" -> databaseTyp = DatabaseTyp.MariaDB;

            case "h2" -> databaseTyp = DatabaseTyp.H2;

            case "h2-server", "h2_server" -> databaseTyp = DatabaseTyp.H2_Server;

            case "postgresql", "postgres" -> databaseTyp = DatabaseTyp.PostgreSQL;

            default -> databaseTyp = DatabaseTyp.SQLite;
        }

        try {
            SQLConfig sqlConfig = SQLConfig.builder()
                    .username(getInstance().getConfig().getConfiguration().getString("hikari.sql.user"))
                    .database(getInstance().getConfig().getConfiguration().getString("hikari.sql.db"))
                    .password(getInstance().getConfig().getConfiguration().getString("hikari.sql.pw"))
                    .host(getInstance().getConfig().getConfiguration().getString("hikari.sql.host"))
                    .port(getInstance().getConfig().getConfiguration().getInt("hikari.sql.port"))
                    .path(getInstance().getConfig().getConfiguration().getString("hikari.misc.storageFile"))
                    .typ(databaseTyp)
                    .poolSize(getInstance().getConfig().getConfiguration().getInt("hikari.misc.poolSize", 1))
                    .createEmbeddedServer(getInstance().getConfig().getConfiguration().getBoolean("hikari.misc.createEmbeddedServer"))
                    .debug(BotConfig.isDebug())
                    .sentry(BotConfig.shouldUseSentry()).build();

            new SQLSession(sqlConfig);
        } catch (Exception exception) {
            log.error("Shutting down, because of an critical error!", exception);
            System.exit(0);
            return;
        }

        log.info("Loading ChatGPTAPI");
        getInstance().setChatGPTAPI(new ChatGPTAPI());

        try {
            // Create the Command-Manager instance.
            getInstance().setCommandManager(new CommandManager());

            Setting prefixSetting = SettingsManager.getDefault("chatprefix");
            SettingsManager.getSettings().remove(prefixSetting);

            prefixSetting.setValue(BotConfig.getDefaultPrefix());
            SettingsManager.getSettings().add(prefixSetting);

            Setting languageSetting = SettingsManager.getDefault("configuration_language");
            SettingsManager.getSettings().remove(languageSetting);

            languageSetting.setValue(BotConfig.getDefaultLanguage());
            SettingsManager.getSettings().add(languageSetting);
        } catch (Exception exception) {
            log.error("Shutting down, because of an critical error!", exception);
            System.exit(0);
            return;
        }

        log.info("Creating JDA Instance.");

        // Create a new Instance of the Bot, as well as add the Events.
        try {
            int shards = getInstance().getConfig().getConfiguration().getInt("bot.misc.shards", 1);

            BotVersion version = BotVersion.RELEASE;

            if (cliArguments.isEnabled("dev")) {
                version = BotVersion.DEVELOPMENT;
            } else if (cliArguments.isEnabled("beta")) {
                version = BotVersion.BETA;
            }

            if (BotConfig.shouldUseLavaLink()) {
                getInstance().lavalink = new JdaLavalink(shards, shard -> BotWorker.getShardManager().getShardById(shard));
            }

            BotWorker.createBot(version, shards);
            getInstance().addEvents();
        } catch (Exception ex) {
            log.error("[Main] Error while init: {}", ex.getMessage());
            Sentry.captureException(ex);
            System.exit(0);
            return;
        }

        try {
            getInstance().setMusicWorker(new MusicWorker());

            if (BotConfig.shouldUseLavaLink()) {

                List<HashMap<String, Object>> nodes = (List<HashMap<String, Object>>) getInstance().getConfig()
                        .getConfiguration().getList("lavalink.nodes");

                for (Config.LavaLinkNodeConfig node : nodes.stream().map(map ->
                        new Config.LavaLinkNodeConfig((String) map.get("name"), (String) map.get("host"),
                                (Integer) map.get("port"), (boolean) map.get("secure"),
                                (String) map.get("password"))).toList()) {
                    getInstance().getLavalink().addNode(node.getName(),
                            URI.create(node.buildAddress()), node.getPassword());
                }
            }
        } catch (Exception ex) {
            log.error("Failed to load Music Module: {}", ex.getMessage());
        }

        if (BotConfig.isModuleActive("music")) {
            log.info("Loading SpotifyAPI");
            new SpotifyAPIHandler();
        }

        if (BotConfig.isModuleActive("games")) {

            if (BotConfig.isModuleActive("music")) {
                log.info("Loading MusicQuizUtil");
                new MusicQuizUtil();
            }

            log.info("Loading GameManager");
            GameManager.loadAllGames();
        }

        if (BotConfig.isModuleActive("streamtools")) {
            log.info("Loading Stream-actions");
            StreamActionContainerCreator.loadAll();
        }

        log.info("Loading GiveawayManager");
        getInstance().setGiveawayManager(new GiveawayManager());

        log.info("Loading InviteContainerManager");
        getInstance().setInviteContainerManager(new InviteContainerManager());

        log.info("Creating Notifier.");

        // Create the Notifier-Manager instance.
        getInstance().setNotifier(new Notifier());

        if (BotConfig.isModuleActive("notifier")) {
            ThreadUtil.createThread(x -> {
                log.info("Loading Notifier data.");
                SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(), "FROM ChannelStats", null).subscribe(channelStats -> {
                    getInstance().getNotifier().getSpotifySonic().load(channelStats);
                    getInstance().getNotifier().getYouTubeSonic().load(channelStats);
                    getInstance().getNotifier().getTwitchSonic().load(channelStats);
                    getInstance().getNotifier().getInstagramSonic().load(channelStats);
                    getInstance().getNotifier().getTwitterSonic().load(channelStats);
                    getInstance().getNotifier().getTikTokSonic().load(channelStats);
                    getInstance().getNotifier().getRssSonic().load(channelStats);
                });
            }, t -> Sentry.captureException(t.getCause()));
        }

        // Add the Runtime-hooks.
        getInstance().addHooks();

        // Set the start Time for stats.
        BotWorker.setStartTime(System.currentTimeMillis());

        log.info("Loading PluginManager");
        // Initialize the Plugin-Manager.
        getInstance().setPluginManager(new ReePluginManager());

        if (BotConfig.isModuleActive("addons")) {
            log.info("Loading all Plugins.");
            getInstance().getPluginManager().loadPlugins();

            log.info("Starting all Plugins.");
            getInstance().getPluginManager().startPlugins();

            log.info("Registering all Commands of plugins.");
            for (PluginWrapper plugin : getInstance().getPluginManager().getPlugins()) {
                if (plugin.getPluginState() != PluginState.STARTED) continue;
                List<ICommand> commands = getInstance().getPluginManager().getExtensions(ICommand.class, plugin.getPluginId());
                log.info("Found {} commands in {}.", commands.size(), plugin.getPluginId());
                commands.forEach(command -> {
                    try {
                        getInstance().getCommandManager().addCommand(command);
                    } catch (CommandInitializerException e) {
                        log.warn("Failed to initialize command: {}", command.getClass().getSimpleName(), e);
                    }
                });
            }
        }

        // Create checker Thread.
        getInstance().createCheckerThread();

        // Create Heartbeat Thread.
        getInstance().createHeartbeatThread();

        log.info("Any previous messages about \"Error executing DDL\" can be most likely ignored.");
        log.info("Initialization finished.");
        log.info("Bot is ready to use.");
        log.info("You are running on: v{}", BotWorker.getBuild());
        log.info("You are running on: {} Shards.", BotWorker.getShardManager().getShardsTotal());
        log.info("You are running on: {} Guilds.", BotWorker.getShardManager().getGuilds().size());
        log.info("You are running on: {} Users.", BotWorker.getShardManager().getUsers().size());
        log.info("Have fun!");
    }

    /**
     * Called to add all Events.
     */
    private void addEvents() {
        BotWorker.addEvent(new MenuEvents(), new OtherEvents());

        if (BotConfig.isModuleActive("logging"))
            BotWorker.addEvent(new LoggingEvents());

        if (BotConfig.isModuleActive("games"))
            BotWorker.addEvent(new GameEvents());

        if (BotConfig.isModuleActive("customevents"))
            BotWorker.getShardManager().addEventListener(new CustomEvents());
    }

    /**
     * Called to add all Runtime-hooks.
     */
    private void addHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Called when the application shutdowns.
     */
    private void shutdown() {
        // Current time for later stats.
        long start = System.currentTimeMillis();
        log.info("[Main] Shutdown init. !");
        BotWorker.setState(BotState.STOPPED);

        if (BotConfig.isModuleActive("temporalvoice")) {
            // Save it all.
            getConfig().getTemporal().set("temporalvoice", ArrayUtil.temporalVoicechannel);
        }

        // Check if there is an SQL-connection if so, shutdown.
        if (SQLSession.getSqlConnector() != null && (SQLSession.getSqlConnector().isConnected())) {
            log.info("[Main] Closing Database Connection!");
            SQLSession.getSqlConnector().close();
            log.info("[Main] Closed Database Connection!");
        }

        if (BotConfig.isModuleActive("addons")) {
            // Shutdown every Addon.
            log.info("[Main] Disabling every Addon!");
            getPluginManager().unloadPlugins();
            log.info("[Main] Every Addon has been disabled!");
        }

        if (BotConfig.isModuleActive("notifier")) {
            // Close the Twitch-Client
            log.info("[Main] Closing Twitch API Instance!");
            getNotifier().getTwitchClient().close();
            log.info("[Main] Twitch API Instance closed!");
        }

        // Shutdown the Bot instance.
        log.info("[Main] JDA Instance shutdown init. !");
        BotWorker.shutdown();
        log.info("[Main] JDA Instance has been shut down!");

        // Inform of how long it took.
        log.info("[Main] Everything has been shut down in {}ms!", System.currentTimeMillis() - start);
        log.info("[Main] Good bye!");
    }

    /**
     * Method used to create all the misc.
     *
     * @param parentPath the path that should be used.
     */
    private static void downloadMisc(String parentPath) {
        try {
            RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.github.com/repos/Ree6-Applications/Ree6/contents/" + parentPath).build()).getAsJsonArray().forEach(jsonElement -> {
                String name = jsonElement.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                String path = jsonElement.getAsJsonObject().getAsJsonPrimitive("path").getAsString();
                String download = jsonElement.getAsJsonObject().get("download_url").isJsonNull() ? null : jsonElement.getAsJsonObject().getAsJsonPrimitive("download_url").getAsString();

                boolean isDirectory = download == null;

                if (isDirectory) {
                    downloadMisc(path);
                    return;
                }

                Path parentFilePath = Path.of(parentPath);
                if (!Files.exists(parentFilePath)) {
                    try {
                        Files.createDirectories(parentFilePath);
                    } catch (IOException e) {
                        log.error("Failed to create directory: {}", parentFilePath);
                    }
                }

                Path filePath = Path.of(path);
                if (Files.exists(filePath)) {
                    return;
                }

                log.info("Downloading file {}!", name);

                try (InputStream inputStream = RequestUtility.request(RequestUtility.Request.builder().url(download).build())) {
                    if (inputStream == null) return;

                    Files.copy(inputStream, filePath);
                } catch (IOException exception) {
                    log.error("An error occurred while downloading the file!", exception);
                }
            });
        } catch (Exception exception) {
            log.error("An error occurred while downloading the files!", exception);
        }
    }

    /**
     * Method creates a Thread used to create a Checker Thread.
     */
    public void createCheckerThread() {
        ThreadUtil.createThread(x -> {

            if (!lastDay.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                // region Update the statistics.
                try {
                    ArrayUtil.messageIDwithMessage.clear();
                    ArrayUtil.messageIDwithUser.clear();

                    BotWorker.getShardManager().getShards().forEach(jda ->
                            BotWorker.setActivity(jda, BotConfig.getStatus(), Activity.ActivityType.CUSTOM_STATUS));

                    log.info("[Stats] ");
                    log.info("[Stats] Today's Stats:");
                    int guildSize = BotWorker.getShardManager().getGuilds().size(), userSize = BotWorker.getShardManager().getGuilds().stream().mapToInt(Guild::getMemberCount).sum();
                    log.info("[Stats] Guilds: {}", guildSize);
                    log.info("[Stats] Overall Users: {}", userSize);
                    log.info("[Stats] ");

                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    SQLSession.getSqlConnector().getSqlWorker().getStatistics(yesterday.getDayOfMonth(), yesterday.getMonthValue(), yesterday.getYear()).subscribe(statistics -> {
                        JsonObject jsonObject = statistics.isPresent() ? statistics.get().getStatsObject() : new JsonObject();
                        JsonObject guildStats = statistics.isPresent() && jsonObject.has("guild") ? jsonObject.getAsJsonObject("guild") : new JsonObject();

                        guildStats.addProperty("amount", guildSize);
                        guildStats.addProperty("users", userSize);

                        jsonObject.add("guild", guildStats);

                        SQLSession.getSqlConnector().getSqlWorker().updateStatistic(jsonObject);
                    });

                    SQLSession.getSqlConnector().getSqlWorker()
                            .getBirthdays().subscribe(birthdayWishes -> {
                                Calendar currentCalendar = Calendar.getInstance();

                                birthdayWishes.stream().filter(birthday -> {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(birthday.getBirthdate());
                                    return calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                            calendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH);
                                }).forEach(birthday -> {
                                    TextChannel textChannel = BotWorker.getShardManager().getTextChannelById(birthday.getChannelId());

                                    if (textChannel != null && textChannel.canTalk())
                                        textChannel.sendMessage(LanguageService.getByGuild(textChannel.getGuild(), "message.birthday.wish", birthday.getUserId()).block()).queue();
                                });
                            });

                    lastDay = new SimpleDateFormat("dd").format(new Date());
                } catch (Exception exception) {
                    log.error("Failed to update statistics!", exception);
                    Sentry.captureException(exception);
                }
                //endregion
            }

            //region Cleanup
            try {
                File storageTemp = new File("storage/tmp/");
                File[] files = storageTemp.listFiles();
                if (files != null) {
                    Arrays.stream(files).forEach(f -> {
                        try {
                            Files.deleteIfExists(f.toPath());
                        } catch (IOException e) {
                            log.error("Couldn't delete file " + f.getName(), e);
                        }
                    });
                }
            } catch (Exception exception) {
                log.error("Failed to clear temporal files!", exception);
                Sentry.captureException(exception);
            }
            //endregion

            //region Schedules Message sending.
            try {
                SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ScheduledMessage(), "FROM ScheduledMessage", null).subscribe(messages -> {
                    for (ScheduledMessage scheduledMessage : messages) {
                        if (!scheduledMessage.isRepeated()) {
                            if (scheduledMessage.getLastExecute() == null) {
                                if (Timestamp.from(Instant.now()).after(Timestamp.from(scheduledMessage.getCreated().toInstant().plusMillis(scheduledMessage.getDelayAmount())))) {

                                    WebhookUtil.sendWebhook(new WebhookMessageBuilder()
                                            .setUsername(BotConfig.getBotName() + "-Scheduler")
                                            .setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl())
                                            .append(scheduledMessage.getMessage()).build(), scheduledMessage.getScheduledMessageWebhook(), WebhookUtil.WebhookTyp.SCHEDULE);

                                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(scheduledMessage).block();
                                }
                            } else {
                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(scheduledMessage).block();
                            }
                        } else {
                            if (scheduledMessage.getLastUpdated() == null) {
                                if (Timestamp.from(Instant.now()).after(Timestamp.from(scheduledMessage.getCreated().toInstant().plusMillis(scheduledMessage.getDelayAmount())))) {

                                    WebhookUtil.sendWebhook(new WebhookMessageBuilder()
                                            .setUsername(BotConfig.getBotName() + "-Scheduler")
                                            .setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl())
                                            .append(scheduledMessage.getMessage()).build(), scheduledMessage.getScheduledMessageWebhook(), WebhookUtil.WebhookTyp.SCHEDULE);

                                    scheduledMessage.setLastExecute(Timestamp.from(Instant.now()));
                                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(scheduledMessage).block();
                                }
                            } else {
                                if (Timestamp.from(Instant.now()).after(Timestamp.from(scheduledMessage.getLastUpdated().toInstant().plusMillis(scheduledMessage.getDelayAmount())))) {

                                    WebhookUtil.sendWebhook(new WebhookMessageBuilder()
                                            .setUsername(BotConfig.getBotName() + "-Scheduler")
                                            .setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl())
                                            .append(scheduledMessage.getMessage()).build(), scheduledMessage.getScheduledMessageWebhook(), WebhookUtil.WebhookTyp.SCHEDULE);

                                    scheduledMessage.setLastExecute(Timestamp.from(Instant.now()));
                                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(scheduledMessage).block();
                                }
                            }
                        }
                    }
                });
            } catch (Exception exception) {
                log.error("Failed to run scheduled Messages.", exception);
                Sentry.captureException(exception);
            }
            //endregion

            //region Giveaway checks
            try {
                ArrayList<Giveaway> toDelete = new ArrayList<>();
                Instant currentTime = Instant.now();
                for (Giveaway giveaway : giveawayManager.getList()) {
                    Instant giveAwayEndingTime = giveaway.getEnding().toInstant();
                    if (giveAwayEndingTime.isBefore(Instant.now()) && giveAwayEndingTime.isAfter(currentTime.minus(1, ChronoUnit.MINUTES))) {
                        Guild guild = BotWorker.getShardManager().getGuildById(giveaway.getGuildId());

                        if (guild == null) {
                            toDelete.add(giveaway);
                            continue;
                        }

                        GuildMessageChannelUnion channel = guild.getChannelById(GuildMessageChannelUnion.class, giveaway.getChannelId());

                        if (channel == null) {
                            toDelete.add(giveaway);
                            continue;
                        }

                        channel.retrieveMessageById(giveaway.getMessageId()).onErrorMap(throwable -> {
                            toDelete.add(giveaway);
                            return null;
                        }).queue(message -> {
                            if (message == null)
                                return;

                            MessageReaction reaction = message.getReaction(Emoji.fromUnicode("U+1F389"));

                            if (reaction == null) {
                                return;
                            }

                            if (!reaction.hasCount()) {
                                return;
                            }

                            if (reaction.getCount() < 2) {
                                return;
                            }

                            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromMessage(message);

                            reaction.retrieveUsers().mapToResult().onErrorMap(throwable -> {
                                messageEditBuilder.setContent(LanguageService.getByGuild(guild, "message.giveaway.reaction.error").block());
                                message.editMessage(messageEditBuilder.build()).queue();
                                toDelete.add(giveaway);
                                return null;
                            }).queue(users -> {
                                if (users == null) {
                                    messageEditBuilder.setContent(LanguageService.getByGuild(guild, "message.giveaway.reaction.less").block());
                                    message.editMessage(messageEditBuilder.build()).queue();
                                    return;
                                }

                                users.onSuccess(userList -> {

                                    if (userList.isEmpty()) {
                                        messageEditBuilder.setContent(LanguageService.getByGuild(guild, "message.giveaway.reaction.none").block());
                                        message.editMessage(messageEditBuilder.build()).queue();
                                        return;
                                    }

                                    if (userList.stream().filter(user -> !user.isBot()).count() < giveaway.getWinners()) {
                                        messageEditBuilder.setContent(LanguageService.getByGuild(guild, "message.giveaway.reaction.less").block());
                                        message.editMessage(messageEditBuilder.build()).queue();
                                        return;
                                    }


                                    Main.getInstance().getGiveawayManager().endGiveaway(giveaway, messageEditBuilder, userList);
                                    message.editMessage(messageEditBuilder.build()).queue();
                                }).onFailure(Sentry::captureException);
                            });
                        });
                    }
                }

                for (Giveaway giveaway : toDelete) {
                    giveawayManager.remove(giveaway);
                }
            } catch (Exception exception) {
                log.error("Failed to run Giveaway checks.", exception);
                Sentry.captureException(exception);
            }
            //endregion

            //region Twitch credentials updater.
            try {
                // Need to load them all.
                if (BotConfig.isModuleActive("notifier"))
                    Main.getInstance().getNotifier().getCredentialManager().load();

                SQLSession.getSqlConnector().getSqlWorker().getEntityList(new TwitchIntegration(), "FROM TwitchIntegration", null).subscribe(integrations -> {
                    for (TwitchIntegration twitchIntegrations : integrations) {

                        CustomOAuth2Credential credential = CustomOAuth2Util.convert(twitchIntegrations);

                        OAuth2Credential originalCredential = new OAuth2Credential("twitch", credential.getAccessToken(), credential.getRefreshToken(), credential.getUserId(), credential.getUserName(), credential.getExpiresIn(), credential.getScopes());

                        if (!Main.getInstance().getNotifier().getTwitchSubscription().containsKey(credential.getUserId())) {
                            PubSubSubscription[] subscriptions = new PubSubSubscription[3];
                            subscriptions[0] = Main.getInstance().getNotifier().getTwitchClient().getPubSub().listenForChannelPointsRedemptionEvents(originalCredential, twitchIntegrations.getChannelId());
                            subscriptions[1] = Main.getInstance().getNotifier().getTwitchClient().getPubSub().listenForSubscriptionEvents(originalCredential, twitchIntegrations.getChannelId());
                            subscriptions[2] = Main.getInstance().getNotifier().getTwitchClient().getPubSub().listenForFollowingEvents(originalCredential, twitchIntegrations.getChannelId());

                            Main.getInstance().getNotifier().getTwitchSubscription().put(credential.getUserId(), subscriptions);
                        }
                    }
                });
            } catch (Exception exception) {
                log.error("Failed to load Twitch Credentials.", exception);
                Sentry.captureException(exception);
            }
            //endregion

            //region Fallback Temporal Voice check.
            ArrayUtil.temporalVoicechannel.forEach(vc -> {
                VoiceChannel voiceChannel = BotWorker.getShardManager().getVoiceChannelById(vc);
                if (voiceChannel == null) {
                    ArrayUtil.temporalVoicechannel.remove(vc);
                } else {
                    if (voiceChannel.getMembers().isEmpty()) {
                        voiceChannel.delete().queue();
                        ArrayUtil.temporalVoicechannel.remove(vc);
                    } else {
                        if (voiceChannel.getMembers().size() == 1) {
                            if (voiceChannel.getMembers().get(0).getUser().isBot()) {
                                voiceChannel.delete().queue();
                                ArrayUtil.temporalVoicechannel.remove(vc);
                            }
                        }
                    }
                }
            });
            //endregion

        }, null, Duration.ofMinutes(1), true, false);
    }

    /**
     * Method creates a Thread which sends a heartbeat to a URL in an x seconds interval.
     */
    public void createHeartbeatThread() {
        String heartbeatUrl = getInstance().getConfig().getConfiguration().getString("heartbeat.url", null);

        if (heartbeatUrl == null || heartbeatUrl.isBlank() || heartbeatUrl.equalsIgnoreCase("none"))
            return;

        ThreadUtil.createThread(x -> {
                    String formattedUrl = heartbeatUrl.replace("%ping%", String.valueOf(BotWorker.getShardManager().getAverageGatewayPing()));
                    try (InputStream ignored = RequestUtility.request(RequestUtility.Request.builder().url(formattedUrl).GET().build())) {
                        Main.getInstance().logAnalytic("Heartbeat sent!");
                    } catch (Exception exception) {
                        log.warn("Heartbeat failed! Reporting to Sentry...");
                        Sentry.captureException(exception);
                    }
                }, Sentry::captureException,
                Duration.ofSeconds(getInstance().getConfig().getConfiguration().getInt("heartbeat.interval", 60)), true, true);
    }

    /**
     * Method used to log analytics.
     *
     * @param message the message that should be logged.
     * @param args    the arguments for the message that should be logged.
     */
    public void logAnalytic(String message, Object... args) {
        if (!BotConfig.isDebug()) return;
        getAnalyticsLogger().debug(message, args);
    }

    /**
     * Retrieve the Instance of the Analytics Logger.
     *
     * @return {@link Logger} Instance of the Analytics Logger.
     */
    public Logger getAnalyticsLogger() {
        return LoggerFactory.getLogger("analytics");
    }

    /**
     * Retrieve the Instance of the Main class.
     *
     * @return {@link Main} Instance of the Main class.
     */
    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }

        return instance;
    }
}
