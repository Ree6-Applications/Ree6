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
 * A Command to activate YouTube Notifications.
 */
@Command(name = "youtubenotifier", description = "command.description.youtubeNotifier", category = Category.COMMUNITY)
public class YouTubeNotifier implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        if (commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.slashNotSupported"));
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        if(commandEvent.getArguments().length == 1) {
            if(commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                StringBuilder end = new StringBuilder("```\n");

                for(String users : SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeChannels(commandEvent.getGuild().getId())) {
                    end.append(users).append("\n");
                }

                end.append("```");

                commandEvent.reply(end.toString(), 10);

            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier list/add/remove"), 5);
            }
        } else if(commandEvent.getArguments().length == 3) {

            if (commandEvent.getMessage().getMentions().getChannels(TextChannel.class).isEmpty() ||
                    !commandEvent.getMessage().getMentions().getChannels(TextChannel.class).get(0).getGuild().getId().equals(commandEvent.getGuild().getId())) {
                commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier add/remove YouTubeChannel #Channel"), 5);
                return;
            }

            String name = commandEvent.getArguments()[1];
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.getMessage().getMentions().getChannels(TextChannel.class).get(0).createWebhook("Ree6-YouTubeNotifier-" + name).queue(w -> SQLSession.getSqlConnector().getSqlWorker().addYouTubeWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken(), name));
                commandEvent.reply(commandEvent.getResource("message.youtubeNotifier.added",name), 5);

                if (!Main.getInstance().getNotifier().isYouTubeRegistered(name)) {
                    Main.getInstance().getNotifier().registerYouTubeChannel(name);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier remove YouTubeChannel"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier add YouTubeChannel #Channel"), 5);
            }
        } else if(commandEvent.getArguments().length == 2) {
            String name = commandEvent.getArguments()[1];
            if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                SQLSession.getSqlConnector().getSqlWorker().removeYouTubeWebhook(commandEvent.getGuild().getId(), name);
                commandEvent.reply(commandEvent.getResource("message.youtubeNotifier.removed",name), 5);

                if (Main.getInstance().getNotifier().isYouTubeRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterYouTubeChannel(name);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier add YouTubeChannel #Channel"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier remove YouTubeChannel"), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.usage","youtubenotifier list/add/remove"), 5);
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
        return new String[] { "yt", "ytnotifier" };
    }
}
