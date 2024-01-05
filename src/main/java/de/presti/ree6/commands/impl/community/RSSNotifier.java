package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.data.RegExUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.regex.Pattern;

/**
 * A Command to activate RSS Notifications.
 */
@Command(name = "rssnotifier", description = "command.description.rssNotifier", category = Category.COMMUNITY)
public class RSSNotifier implements ICommand {

    /**
     * The Pattern for Links.
     */
    private static final Pattern pattern = Pattern.compile(RegExUtil.URL_REGEX);

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
        OptionMapping nameMapping = commandEvent.getOption("url");
        OptionMapping channelMapping = commandEvent.getOption("channel");

        switch (command) {
            case "list" -> {
                StringBuilder end = new StringBuilder();

                for (String users : SQLSession.getSqlConnector().getSqlWorker().getAllRSSUrls(commandEvent.getGuild().getIdLong())) {
                    end.append(users).append("\n");
                }

                commandEvent.reply(commandEvent.getResource("message.rssNotifier.list", end.toString()), 10);
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

                if (!pattern.matcher(name).matches()) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidUrl"), 5);
                    return;
                }

                StandardGuildMessageChannel channel = channelMapping.getAsChannel().asStandardGuildMessageChannel();
                channel.createWebhook(BotConfig.getBotName() + "-RSSNotifier-" + name).queue(w ->
                        SQLSession.getSqlConnector().getSqlWorker().addRSSWebhook(commandEvent.getGuild().getIdLong(), channel.getIdLong(), w.getIdLong(), w.getToken(), name.toLowerCase()));
                commandEvent.reply(commandEvent.getResource("message.rssNotifier.added", name), 5);

                if (!Main.getInstance().getNotifier().isRSSRegistered(name)) {
                    Main.getInstance().getNotifier().registerRSS(name);
                }
            }
            case "remove" -> {
                if (nameMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                String name = nameMapping.getAsString();
                SQLSession.getSqlConnector().getSqlWorker().removeRSSWebhook(commandEvent.getGuild().getIdLong(), name);
                commandEvent.reply(commandEvent.getResource("message.rssNotifier.removed", name), 5);

                if (Main.getInstance().getNotifier().isRSSRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterRSS(name);
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
        return new CommandDataImpl("rssnotifier", "command.description.rssNotifier")
                .addSubcommands(new SubcommandData("list", "List all RSS Feeds."))
                .addSubcommands(new SubcommandData("add", "Add a RSS Notifier for a specific url.")
                        .addOption(OptionType.STRING, "url", "The RSS url.", true)
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT)))
                        .addSubcommands(new SubcommandData("remove", "Remove a RSS Notifier for a specific url.")
                                .addOption(OptionType.STRING, "url", "The RSS url of the Notifier.", true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
