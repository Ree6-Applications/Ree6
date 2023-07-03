package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.webhook.WebhookTikTok;
import de.presti.ree6.utils.data.Data;
import de.presti.wrapper.tiktok.TikTokWrapper;
import de.presti.wrapper.tiktok.entities.TikTokUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Map;

/**
 * A Command to activate TikTok Notifications.
 */
@Command(name = "tiktoknotifier", description = "command.description.tiktokNotifier", category = Category.COMMUNITY)
public class TikTokNotifier implements ICommand {

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

                for (String users : SQLSession.getSqlConnector().getSqlWorker().getAllTikTokNames(commandEvent.getGuild().getId())) {
                    end.append(users).append("\n");
                }

                commandEvent.reply(commandEvent.getResource("message.tiktokNotifier.list", end.toString()), 10);
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

                try {
                    TikTokUser tikTokUser = TikTokWrapper.getUser(name, false);

                    StandardGuildMessageChannel channel = channelMapping.getAsChannel().asStandardGuildMessageChannel();

                    channel.createWebhook(Data.getBotName() + "-TikTokNotifier-" + name).queue(w -> {
                        if (messageMapping != null) {
                            SQLSession.getSqlConnector().getSqlWorker().addTikTokWebhook(commandEvent.getGuild().getId(), channel.getIdLong(), w.getId(), w.getToken(), tikTokUser.getId(), messageMapping.getAsString());
                        } else {
                            SQLSession.getSqlConnector().getSqlWorker().addTikTokWebhook(commandEvent.getGuild().getId(), channel.getIdLong(), w.getId(), w.getToken(), tikTokUser.getId());
                        }
                    });
                    commandEvent.reply(commandEvent.getResource("message.tiktokNotifier.added", name), 5);

                    if (!Main.getInstance().getNotifier().isTikTokUserRegistered(Long.parseLong(tikTokUser.getId()))) {
                        Main.getInstance().getNotifier().registerTikTokUser(Long.parseLong(tikTokUser.getId()));
                    }
                } catch (Exception e) {
                    commandEvent.reply(commandEvent.getResource("message.tiktokNotifier.invalidUser", name), 5);
                }
            }
            case "remove" -> {
                if (nameMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                String name = nameMapping.getAsString();

                try {
                    TikTokUser tikTokUser = TikTokWrapper.getUser(name, false);

                    SQLSession.getSqlConnector().getSqlWorker().removeTikTokWebhook(commandEvent.getGuild().getId(), tikTokUser.getId());

                    commandEvent.reply(commandEvent.getResource("message.tiktokNotifier.removed", name), 5);

                    if (Main.getInstance().getNotifier().isTikTokUserRegistered(Long.parseLong(tikTokUser.getId()))) {
                        Main.getInstance().getNotifier().unregisterTikTokUser(Long.parseLong(tikTokUser.getId()));
                    }
                } catch (Exception e) {
                    commandEvent.reply(commandEvent.getResource("message.tiktokNotifier.invalidUser", name), 5);
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
        return new CommandDataImpl("tiktoknotifier", "command.description.tiktokNotifier")
                .addSubcommands(new SubcommandData("list", "List all TikTok users."))
                .addSubcommands(new SubcommandData("add", "Add a TikTok Notifier for a specific user.")
                        .addOption(OptionType.STRING, "name", "The TikTok username.", true)
                        .addOption(OptionType.CHANNEL, "channel", "The channel.", true)
                        .addOption(OptionType.STRING, "message", "Custom announcement message.", false))
                .addSubcommands(new SubcommandData("remove", "Remove a TikTok Notifier for a specific user.")
                        .addOption(OptionType.STRING, "name", "The TikTok username of the Notifier.", true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
