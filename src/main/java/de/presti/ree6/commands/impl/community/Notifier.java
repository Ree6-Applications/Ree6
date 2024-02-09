package de.presti.ree6.commands.impl.community;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A Command to activate Notifications.
 */
@Command(name = "notifier", description = "command.description.instagramNotifier", category = Category.COMMUNITY)
public class Notifier implements ICommand {

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

                for (String users : SQLSession.getSqlConnector().getSqlWorker().getAllInstagramUsers(commandEvent.getGuild().getIdLong())) {
                    end.append(users).append("\n");
                }

                commandEvent.reply(commandEvent.getResource("message.instagramNotifier.list", end.toString()), 10);
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
                channel.createWebhook(BotConfig.getBotName() + "-InstagramNotifier-" + name).queue(w -> {
                    if (messageMapping != null) {
                        SQLSession.getSqlConnector().getSqlWorker().addInstagramWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase(), messageMapping.getAsString());
                    } else {
                        SQLSession.getSqlConnector().getSqlWorker().addInstagramWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase());
                    }
                });
                commandEvent.reply(commandEvent.getResource("message.instagramNotifier.added", name), 5);

                if (!Main.getInstance().getNotifier().isInstagramUserRegistered(name)) {
                    Main.getInstance().getNotifier().registerInstagramUser(name);
                }
            }
            case "remove" -> {
                if (nameMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                String name = nameMapping.getAsString();
                SQLSession.getSqlConnector().getSqlWorker().removeInstagramWebhook(commandEvent.getGuild().getIdLong(), name);
                commandEvent.reply(commandEvent.getResource("message.instagramNotifier.removed", name), 5);

                if (Main.getInstance().getNotifier().isInstagramUserRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterInstagramUser(name);
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
        return new CommandDataImpl("notifier", "command.description.instagramNotifier")
                .addSubcommandGroups(
                        new SubcommandGroupData("instagram", "Instagram Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all Instagram users."),
                                        new SubcommandData("add", "Add a Instagram Notifier for a specific user.")
                                                .addOption(OptionType.STRING, "name", "The username.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                                                .addOption(OptionType.STRING, "message", "Custom announcement message.", false),
                                        new SubcommandData("remove", "Remove a Instagram Notifier for a specific user.")
                                                .addOption(OptionType.STRING, "name", "The username of the Notifier.", true)),

                        new SubcommandGroupData("youtube", "YouTube Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all YouTube channels."),
                                        new SubcommandData("add", "Add a YouTube Notifier for a specific channel.")
                                                .addOption(OptionType.STRING, "url", "The YouTube channel url.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                                                .addOption(OptionType.STRING, "message", "Custom announcement message.", false),
                                        new SubcommandData("remove", "Remove a YouTube Notifier for a specific channel.")
                                                .addOption(OptionType.STRING, "name", "The YouTube channel name or id of the Notifier.", true)),

                        new SubcommandGroupData("twitch", "Twitch Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all Twitch channels."),
                                        new SubcommandData("add", "Add a Twitch Notifier for a specific channel.")
                                                .addOption(OptionType.STRING, "name", "The Twitch channel name.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                                                .addOption(OptionType.STRING, "message", "Custom announcement message.", false),
                                        new SubcommandData("remove", "Remove a Twitch Notifier for a specific channel.")
                                                .addOption(OptionType.STRING, "name", "The Twitch channel name of the Notifier.", true)),

                        new SubcommandGroupData("twitter", "Twitter Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all Twitter users."),
                                        new SubcommandData("add", "Add a Twitter Notifier for a specific user.")
                                                .addOption(OptionType.STRING, "name", "The Twitter username.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                                                .addOption(OptionType.STRING, "message", "Custom announcement message.", false),
                                        new SubcommandData("remove", "Remove a Twitter Notifier for a specific user.")
                                                .addOption(OptionType.STRING, "name", "The Twitter username of the Notifier.", true)),

                        new SubcommandGroupData("tiktok", "TikTok Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all TikTok users."),
                                        new SubcommandData("add", "Add a TikTok Notifier for a specific user.")
                                                .addOption(OptionType.STRING, "name", "The TikTok username.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                                                .addOption(OptionType.STRING, "message", "Custom announcement message.", false),
                                        new SubcommandData("remove", "Remove a TikTok Notifier for a specific user.")
                                                .addOption(OptionType.STRING, "name", "The TikTok username of the Notifier.", true)),

                        new SubcommandGroupData("reddit", "Reddit Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all subreddits."),
                                        new SubcommandData("add", "Add a Reddit Notifier for a specific subreddit.")
                                                .addOption(OptionType.STRING, "name", "The subreddit name.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                                                .addOption(OptionType.STRING, "message", "Custom announcement message.", false),
                                        new SubcommandData("remove", "Remove a Reddit Notifier for a specific subreddit.")
                                                .addOption(OptionType.STRING, "name", "The subreddit name of the Notifier.", true)),

                        new SubcommandGroupData("rss", "RSS Notifier")
                                .addSubcommands(
                                        new SubcommandData("list", "List all RSS Feeds."),
                                        new SubcommandData("add", "Add a RSS Notifier for a specific url.")
                                                .addOption(OptionType.STRING, "url", "The RSS url.", true)
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT)),
                                        new SubcommandData("remove", "Remove a RSS Notifier for a specific url.")
                                                .addOption(OptionType.STRING, "url", "The RSS url of the Notifier.", true)));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"instanotifier", "insta", "instagram"};
    }
}
