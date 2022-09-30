package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A Command to activate Reddit Notifications.
 */
@Command(name = "redditnotifier", description = "Manage your Reddit-Notifier!", category = Category.COMMUNITY)
public class RedditNotifier implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            Main.getInstance().getCommandManager().sendMessage("I need the permission `Manage Webhooks` to use this command!", commandEvent.getChannel(), commandEvent.getInteractionHook());
        }

        if (commandEvent.isSlashCommand()) {
            Main.getInstance().getCommandManager().sendMessage("This Command doesn't support slash commands yet.", commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if(commandEvent.getArguments().length == 1) {
            if(commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                StringBuilder end = new StringBuilder("```\n");

                for(String users : Main.getInstance().getSqlConnector().getSqlWorker().getAllSubreddits(commandEvent.getGuild().getId())) {
                    end.append(users).append("\n");
                }

                end.append("```");

                Main.getInstance().getCommandManager().sendMessage(end.toString(), 10, commandEvent.getChannel(), commandEvent.getInteractionHook());

            } else {
                Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier list/add/remove", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else if(commandEvent.getArguments().length == 3) {

            if (commandEvent.getMessage().getMentions().getChannels(TextChannel.class).isEmpty() ||
                    !commandEvent.getMessage().getMentions().getChannels(TextChannel.class).get(0).getGuild().getId().equals(commandEvent.getGuild().getId())) {
                Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier add/remove Subreddit #Channel", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                return;
            }

            String name = commandEvent.getArguments()[1];
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.getMessage().getMentions().getChannels(TextChannel.class).get(0).createWebhook("Ree6-RedditNotifier-" + name).queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().addRedditWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken(), name));
                Main.getInstance().getCommandManager().sendMessage("A Reddit Notifier has been created for the Subreddit " + name + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());

                if (!Main.getInstance().getNotifier().isSubredditRegistered(name)) {
                    Main.getInstance().getNotifier().registerSubreddit(name);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier remove Subreddit", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier add Subreddit #Channel", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else if(commandEvent.getArguments().length == 2) {
            String name = commandEvent.getArguments()[1];
            if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                Main.getInstance().getSqlConnector().getSqlWorker().removeRedditWebhook(commandEvent.getGuild().getId(), name);
                Main.getInstance().getCommandManager().sendMessage("A Reddit Notifier has been removed from the Subreddit " + name + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());

                if (Main.getInstance().getNotifier().isSubredditRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterSubreddit(name);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier add Subreddit #Channel", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier remove Subreddit", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "redditnotifier list/add/remove", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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
        return new String[] { "reddit", "reditnotifier" };
    }
}
