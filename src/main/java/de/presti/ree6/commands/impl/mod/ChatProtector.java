package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;

public class ChatProtector extends Command {

    public ChatProtector() {
        super("chatprotector", "Manage the Chat Filter!", Category.MOD, new String[]{ "blacklist", "badword" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            sendMessage("This Command doesn't support slash commands yet.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if(commandEvent.getArguments().length >= 1) {
                if(commandEvent.getArguments().length == 1) {
                    if(commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                        sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add WORD WORD2 WORD3 AND MORE WORDS", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector remove WORD", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else if (commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                        if(de.presti.ree6.addons.impl.ChatProtector.hasChatProtector(commandEvent.getGuild().getId())) {
                            StringBuilder end = new StringBuilder();

                            for (String s : de.presti.ree6.addons.impl.ChatProtector.getChatProtector(commandEvent.getGuild().getId())) {
                                end.append("\n").append(s);
                            }

                            sendMessage("```" + end + "```", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        } else {
                            sendMessage("Your ChatProtector isn't setuped!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add WORD WORD2 WORD3 AND MORE WORDS", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        }
                    } else {
                        sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add/remove/list", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else {
                    if(commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                        if(commandEvent.getArguments().length > 2) {
                            StringBuilder end = new StringBuilder();
                            ArrayList<String> words = new ArrayList<>();
                            for(int i = 2; i < commandEvent.getArguments().length; i++) {
                                words.add(commandEvent.getArguments()[i]);
                                end.append("\n").append(commandEvent.getArguments()[i]);
                            }
                            de.presti.ree6.addons.impl.ChatProtector.addWordsToProtector(commandEvent.getGuild().getId(), words);
                            sendMessage("The Wordlist has been added to your ChatProtector!\nYour Wordlist:\n```" + end + "```", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        } else {
                            de.presti.ree6.addons.impl.ChatProtector.addWordToProtector(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                            sendMessage("The Word " + commandEvent.getArguments()[1] + " has been added to your ChatProtector!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        }
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        de.presti.ree6.addons.impl.ChatProtector.removeWordFromProtector(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                        sendMessage("The Word " + commandEvent.getArguments()[1] + " has been removed from your ChatProtector!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add/remove/list", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                }
            } else {
                sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add/remove/list", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
