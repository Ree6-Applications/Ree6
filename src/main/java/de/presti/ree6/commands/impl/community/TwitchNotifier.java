package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;

public class TwitchNotifier extends Command {


    public TwitchNotifier() {
        super("twitch", "Manage your Twitchnotifier!", Category.COMMUNITY, new String[]{ "twitchnotifier", "streamnotifier"});
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

                for(String users : Main.getInstance().getSqlConnector().getSqlWorker().getAllTwitchNames(commandEvent.getGuild().getId())) {
                    end.append(users).append("\n");
                }

                end.append("```");

                sendMessage(end.toString(), 10, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

            } else {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitch list/add/remove", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else if(commandEvent.getArguments().length == 3) {

            if (commandEvent.getMessage().getMentionedChannels().size() == 0) {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitch add/remove TwitchName #Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                return;
            }

            String name = commandEvent.getArguments()[1];
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                commandEvent.getMessage().getMentionedChannels().get(0).createWebhook("Ree6-TwitchNotifier-" + name).queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().addTwitchWebhook(commandEvent.getGuild().getId(), name.toLowerCase(), w.getId(), w.getToken()));
                sendMessage("A TwitchStream Notifier has been created for the User " + name + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

                if (!Main.getInstance().getNotifier().isTwitchRegistered(name)) {
                    Main.getInstance().getNotifier().registerTwitchChannel(name);
                }
            } else {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitch add TwitchName #Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else if(commandEvent.getArguments().length == 2) {
            String name = commandEvent.getArguments()[1];
            if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                Main.getInstance().getSqlConnector().getSqlWorker().removeTwitchWebhook(commandEvent.getGuild().getId(), name);
                sendMessage("A TwitchStream Notifier has been removed from the User " + name + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

                if(Main.getInstance().getNotifier().isTwitchRegistered(name)) {
                    if(Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(name.toLowerCase()).isEmpty()) {
                        Main.getInstance().getNotifier().unregisterTwitchChannel(name);
                    }
                }
            } else {
                sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitch remove TwitchName", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            sendMessage("Please use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitch list/add/remove", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
