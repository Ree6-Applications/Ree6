package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.bot.BotConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A Command to activate Reddit Notifications.
 */
@Command(name = "redditnotifier", description = "command.description.redditNotifier", category = Category.COMMUNITY)
public class RedditNotifier implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        String command = commandEvent.getSubcommand();
        OptionMapping nameMapping = commandEvent.getOption("name");
        OptionMapping channelMapping = commandEvent.getOption("channel");
        OptionMapping messageMapping = commandEvent.getOption("message");

        switch (command) {
            case "list" -> {
                StringBuilder end = new StringBuilder();

                for (String users : SQLSession.getSqlConnector().getSqlWorker().getAllSubreddits(commandEvent.getGuild().getIdLong())) {
                    end.append(users).append("\n");
                }

                commandEvent.reply(commandEvent.getResource("message.redditNotifier.list", end.toString()), 10);
            }
            case "add" -> {
                if (nameMapping == null || channelMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                if (!channelMapping.getAsChannel().getGuild().getId().equals(commandEvent.getGuild().getId())) {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.channel"), 5);
                    return;
                }

                String name = nameMapping.getAsString();
                StandardGuildMessageChannel channel = channelMapping.getAsChannel().asStandardGuildMessageChannel();
                channel.createWebhook(BotConfig.getBotName() + "-RedditNotifier-" + name).queue(w -> {
                    if (messageMapping != null) {
                        SQLSession.getSqlConnector().getSqlWorker().addRedditWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase(), messageMapping.getAsString());
                    } else {
                        SQLSession.getSqlConnector().getSqlWorker().addRedditWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase());
                    }
                });
                commandEvent.reply(commandEvent.getResource("message.redditNotifier.added", name), 5);

                if (!Main.getInstance().getNotifier().isSubredditRegistered(name)) {
                    Main.getInstance().getNotifier().registerSubreddit(name);
                }
            }
            case "remove" -> {
                if (nameMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                String name = nameMapping.getAsString();
                SQLSession.getSqlConnector().getSqlWorker().removeRedditWebhook(commandEvent.getGuild().getIdLong(), name);
                commandEvent.reply(commandEvent.getResource("message.redditNotifier.removed", name), 5);

                if (Main.getInstance().getNotifier().isSubredditRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterSubreddit(name);
                }
            }

            default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("redditnotifier", "command.description.redditNotifier")
                .addSubcommands(new SubcommandData("list", "List all subreddits."))
                .addSubcommands(new SubcommandData("add", "Add a Reddit Notifier for a specific subreddit.")
                        .addOption(OptionType.STRING, "name", "The subreddit name.", true)
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                        .addOption(OptionType.STRING, "message", "Custom announcement message.", false))
                .addSubcommands(new SubcommandData("remove", "Remove a Reddit Notifier for a specific subreddit.")
                        .addOption(OptionType.STRING, "name", "The subreddit name of the Notifier.", true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "reddit", "reditnotifier" };
    }
}
