package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;

public class TwitterNotifier extends Command {

    public TwitterNotifier() {
        super("twitternotifier", "Twitter Notifier", Category.COMMUNITY);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            sendMessage("This Command doesn't support slash commands yet.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        if(commandEvent.getArguments().length == 1) {
            if(commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                StringBuilder end = new StringBuilder("```\n");

                for(String users : Main.getInstance().getSqlConnector().getSqlWorker().getAllTwitterNames(commandEvent.getGuild().getId())) {
                    end.append(users).append("\n");
                }

                end.append("```");

                sendMessage(end.toString(), 10, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

            } else {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitternotifier list/add/remove", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else if(commandEvent.getArguments().length == 3) {

            if (commandEvent.getMessage().getMentionedChannels().isEmpty()) {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitternotifier add/remove TwitterName #Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                return;
            }

            String name = commandEvent.getArguments()[1];
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.getMessage().getMentionedChannels().get(0).createWebhook("Ree6-TwitterNotifier-" + name).queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().addTwitterWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken(), name.toLowerCase()));
                sendMessage("A TwitterTweet Notifier has been created for the User " + name + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

                if (!Main.getInstance().getNotifier().isTwitterRegistered(name)) {
                    Main.getInstance().getNotifier().registerTwitterUser(name);
                }
            } else {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitternotifier add TwitterName #Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else if(commandEvent.getArguments().length == 2) {
            String name = commandEvent.getArguments()[1];
            if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                Main.getInstance().getSqlConnector().getSqlWorker().removeTwitterWebhook(commandEvent.getGuild().getId(), name);
                sendMessage("A TwitterTweet Notifier has been removed from the User " + name + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

                if(Main.getInstance().getNotifier().isTwitterRegistered(name)) {
                    Main.getInstance().getNotifier().unregisterTwitterUser(name);
                }
            } else {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitternotifier remove TwitterName", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitternotifier list/add/remove", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
