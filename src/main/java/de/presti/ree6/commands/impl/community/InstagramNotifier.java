package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A Command to activate Instagram Notifications.
 */
@Command(name = "instagramnotifier", description = "command.description.instagramNotifier", category = Category.COMMUNITY)
public class InstagramNotifier implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
        }

        if (commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.slashNotSupported"));
            return;
        }

        if(commandEvent.getArguments().length == 1) {
            if(commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                StringBuilder end = new StringBuilder("```\n");

                for(String users : SQLSession.getSqlConnector().getSqlWorker().getAllInstagramUsers(commandEvent.getGuild().getId())) {
                    end.append(users).append("\n");
                }

                end.append("```");

                commandEvent.reply(end.toString(), 10);

            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage", "instagramnotifier list/add/remove"), 5);
            }
        } else if(commandEvent.getArguments().length == 3) {

            if (commandEvent.getMessage().getMentions().getChannels(TextChannel.class).isEmpty() ||
                    !commandEvent.getMessage().getMentions().getChannels(TextChannel.class).get(0).getGuild().getId().equals(commandEvent.getGuild().getId())) {
                commandEvent.reply("message.default.noMention.channel", 5);
                return;
            }

            String name = commandEvent.getArguments()[1];
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.getMessage().getMentions().getChannels(TextChannel.class).get(0).createWebhook("Ree6-InstagramNotifier-" + name).queue(w -> SQLSession.getSqlConnector().getSqlWorker().addInstagramWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken(), name.toLowerCase()));
                commandEvent.reply(commandEvent.getResource("message.instagramNotifier.added", name), 5);

                if (!Main.getInstance().getNotifier().isInstagramUserRegistered(name)) {
                    Main.getInstance().getNotifier().registerInstagramUser(name);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                commandEvent.reply(commandEvent.getResource("message.default.usage", "instagramnotifier remove InstagramName"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage", "instagramnotifier add InstagramName #Channel"), 5);
            }
        } else if(commandEvent.getArguments().length == 2) {
            String name = commandEvent.getArguments()[1];
            if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                SQLSession.getSqlConnector().getSqlWorker().removeInstagramWebhook(commandEvent.getGuild().getId(), name);
                commandEvent.reply(commandEvent.getResource("message.instagramNotifier.removed", name), 5);

                if (Main.getInstance().getNotifier().isInstagramUserRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterInstagramUser(name);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.reply(commandEvent.getResource("message.default.usage","instagramnotifier add InstagramName #Channel"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","instagramnotifier remove InstagramName"), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.usage","instagramnotifier list/add/remove"), 5);
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "instanotifier", "insta", "instagram" };
    }
}
