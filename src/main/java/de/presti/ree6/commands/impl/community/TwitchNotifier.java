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
 * A Command to activate Twitch Notifications.
 */
@Command(name = "twitch", description = "command.description.twitch", category = Category.COMMUNITY)
public class TwitchNotifier implements ICommand {

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

                for (String users : SQLSession.getSqlConnector().getSqlWorker().getAllTwitchNames(commandEvent.getGuild().getIdLong())) {
                    end.append(users).append("\n");
                }

                commandEvent.reply(commandEvent.getResource("message.twitchNotifier.list", end.toString()), 10);
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
                channel.createWebhook(BotConfig.getBotName() + "-TwitchNotifier-" + name).queue(w -> {
                    if (messageMapping != null) {
                        SQLSession.getSqlConnector().getSqlWorker().addTwitchWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase(), messageMapping.getAsString());
                    } else {
                        SQLSession.getSqlConnector().getSqlWorker().addTwitchWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase());
                    }
                });
                commandEvent.reply(commandEvent.getResource("message.twitchNotifier.added", name), 5);

                if (!Main.getInstance().getNotifier().isTwitchRegistered(name)) {
                    Main.getInstance().getNotifier().registerTwitchChannel(name);
                }
            }
            case "remove" -> {
                if (nameMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                String name = nameMapping.getAsString();
                SQLSession.getSqlConnector().getSqlWorker().removeTwitchWebhook(commandEvent.getGuild().getIdLong(), name);
                commandEvent.reply(commandEvent.getResource("message.twitchNotifier.removed", name), 5);

                if (Main.getInstance().getNotifier().isTwitchRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterTwitchChannel(name);
                }
            }

            default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"twitchnotifier", "streamnotifier"};
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("twitch", "command.description.twitch")
                .addSubcommands(new SubcommandData("list", "List all Twitch channels."))
                .addSubcommands(new SubcommandData("add", "Add a Twitch Notifier for a specific channel.")
                        .addOption(OptionType.STRING, "name", "The Twitch channel name.", true)
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT))
                        .addOption(OptionType.STRING, "message", "Custom announcement message.", false))
                .addSubcommands(new SubcommandData("remove", "Remove a Twitch Notifier for a specific channel.")
                        .addOption(OptionType.STRING, "name", "The Twitch channel name of the Notifier.", true));
    }
}
