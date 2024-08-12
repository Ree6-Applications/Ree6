package de.presti.ree6.commands.interfaces;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.news.AnnouncementManager;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An Interface class, used to make it easier for the creation of Commands.
 */
public interface ICommand {

    /**
     * The Logger for this class.
     */
    Logger log = LoggerFactory.getLogger(ICommand.class);

    default Mono<Boolean> onMonoPerform(CommandEvent commandEvent) {
        if (BotConfig.isDebug())
            log.info("Command {} called by {} in {} ({}).", commandEvent.getCommand(), commandEvent.getMember().getUser().getName(), commandEvent.getGuild().getName(), commandEvent.getGuild().getId());

        return Mono.fromRunnable(() -> onPerformWithLog(commandEvent)).thenReturn(true).onErrorResume(throwable -> {
            if (!throwable.getMessage().contains("Unknown Message")) {
                commandEvent.reply(commandEvent.getResource("command.perform.internalError"), 5);
                log.error("An error occurred while executing the command!", throwable);
                Sentry.captureException(throwable);
            }
            return Mono.just(false);
        }).doOnSuccess(success -> {
            if (BotConfig.isDebug())
                log.info("Updating Stats {} in {} ({}).", commandEvent.getCommand(), commandEvent.getGuild().getName(), commandEvent.getGuild().getId());
            // Update Stats.
            SQLSession.getSqlConnector().getSqlWorker().addStats(commandEvent.getGuild().getIdLong(), commandEvent.getCommand());
            Optional<Setting> setting = SQLSession.getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getIdLong(), "configuration_news").block();
            if (setting == null || setting.isEmpty() || !setting.get().getBooleanValue()) return;
            AnnouncementManager.getAnnouncementList().forEach(a -> {
                if (!AnnouncementManager.hasReceivedAnnouncement(commandEvent.getGuild().getIdLong(), a.id())) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setTitle(a.title())
                            .setAuthor(BotConfig.getBotName() + "-Info")
                            .setDescription(a.content().replace("\\n", "\n") + "\n\n" + LanguageService.getByGuild(commandEvent.getGuild(), "message.news.notice").block())
                            .setFooter(BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                            .setColor(BotWorker.randomEmbedColor()), 15, commandEvent.getChannel());

                    AnnouncementManager.addReceivedAnnouncement(commandEvent.getGuild().getIdLong(), a.id());
                }
            });
        });
    }

    /**
     * Will be fired when the Command is called.
     *
     * @param commandEvent the Event, with every necessary data.
     * @deprecated Use {@link #onMonoPerform(CommandEvent)} instead.
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    default void onASyncPerform(CommandEvent commandEvent) {
        CompletableFuture.runAsync(() -> onPerform(commandEvent)).exceptionally(throwable -> {
            if (!throwable.getMessage().contains("Unknown Message")) {
                commandEvent.reply(commandEvent.getResource("command.perform.internalError"), 5);
                log.error("An error occurred while executing the command!", throwable);
                Sentry.captureException(throwable);
            }
            return null;
        });

        // Update Stats.
        SQLSession.getSqlConnector().getSqlWorker().addStats(commandEvent.getGuild().getIdLong(), commandEvent.getCommand());
        SQLSession.getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getIdLong(), "configuration_news").subscribe(setting -> {
            if (setting.isEmpty() || !setting.get().getBooleanValue()) return;
            AnnouncementManager.getAnnouncementList().forEach(a -> {
                if (!AnnouncementManager.hasReceivedAnnouncement(commandEvent.getGuild().getIdLong(), a.id())) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setTitle(a.title())
                            .setAuthor(BotConfig.getBotName() + "-Info")
                            .setDescription(a.content().replace("\\n", "\n") + "\n\n" + LanguageService.getByGuild(commandEvent.getGuild(), "message.news.notice").block())
                            .setFooter(BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                            .setColor(BotWorker.randomEmbedColor()), 15, commandEvent.getChannel());

                    AnnouncementManager.addReceivedAnnouncement(commandEvent.getGuild().getIdLong(), a.id());
                }
            });
        });
    }

    default void onPerformWithLog(CommandEvent commandEvent) {
        onPerform(commandEvent);
        if (BotConfig.isDebug())
            log.info("Command {} has ended, called by {} in {} ({}).", commandEvent.getCommand(), commandEvent.getMember().getUser().getName(), commandEvent.getGuild().getName(), commandEvent.getGuild().getId());
    }

    /**
     * Will be fired when the Command is called.
     *
     * @param commandEvent the Event, with every necessary data.
     */
    void onPerform(CommandEvent commandEvent);

    /**
     * A CommandData implementation for JDAs SlashCommand Interaction Implementation.
     *
     * @return the created CommandData.
     */
    CommandData getCommandData();

    /**
     * Aliases of the current Command.
     *
     * @return the Aliases.
     */
    String[] getAlias();

}
