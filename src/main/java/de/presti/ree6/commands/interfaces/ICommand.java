package de.presti.ree6.commands.interfaces;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.news.AnnouncementManager;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.others.ThreadUtil;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * An Interface class, used to make it easier for the creation of Commands.
 */
public interface ICommand {

    /**
     * The Logger for this class.
     */
    Logger log = LoggerFactory.getLogger(ICommand.class);

    /**
     * Will be fired when the Command is called.
     *
     * @param commandEvent the Event, with every needed data.
     */
    default void onASyncPerform(CommandEvent commandEvent) {
        CompletableFuture.runAsync(() -> onPerform(commandEvent)).exceptionally(throwable -> {
            if (!throwable.getMessage().contains("Unknown Message")) {
                commandEvent.reply(commandEvent.getResource("command.perform.internalError"), 5);
                log.error("An error occurred while executing the command!", throwable);
                Sentry.captureException(throwable);
            }
            return null;
        });
        ThreadUtil.createThread(y -> {
            // Update Stats.
            SQLSession.getSqlConnector().getSqlWorker().addStats(commandEvent.getGuild().getIdLong(), commandEvent.getCommand());
            if (SQLSession.getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getIdLong(), "configuration_news").getBooleanValue()) {
                AnnouncementManager.getAnnouncementList().forEach(a -> {
                    if (!AnnouncementManager.hasReceivedAnnouncement(commandEvent.getGuild().getIdLong(), a.id())) {
                        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setTitle(a.title())
                                .setAuthor(BotConfig.getBotName() + "-Info")
                                .setDescription(a.content().replace("\\n", "\n") + "\n\n" + LanguageService.getByGuild(commandEvent.getGuild(), "message.news.notice"))
                                .setFooter(BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                                .setColor(BotWorker.randomEmbedColor()), 15, commandEvent.getChannel());

                        AnnouncementManager.addReceivedAnnouncement(commandEvent.getGuild().getIdLong(), a.id());
                    }
                });
            }
        });
    }

    /**
     * Will be fired when the Command is called.
     *
     * @param commandEvent the Event, with every needed data.
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
