package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;

public class ChatProtector extends Command {

    public ChatProtector() {
        super("chatprotector", "Manage the Chat Filter!", Category.MOD, new String[]{ "cp" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if(args.length >= 1) {
                if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("add")) {
                        sendMessage("Not enough Arguments!", 5, m);
                        sendMessage("Use ree!chatprotector add WORD WORD2 WORD3 AND MORE WORDS", 5, m);
                    } else if(args[0].equalsIgnoreCase("remove")) {
                        sendMessage("Not enough Arguments!", 5, m);
                        sendMessage("Use ree!chatprotector remove WORD", 5, m);
                    } else if (args[0].equalsIgnoreCase("list")) {
                        if(de.presti.ree6.addons.impl.ChatProtector.hasChatProtector(m.getGuild().getId())) {
                            StringBuilder end = new StringBuilder();

                            for (String s : de.presti.ree6.addons.impl.ChatProtector.getChatProtector(m.getGuild().getId())) {
                                end.append("\n").append(s);
                            }

                            sendMessage("```" + end + "```", m);
                        } else {
                            sendMessage("Your ChatProtector isn't setuped!", 5, m);
                            sendMessage("Use ree!chatprotector add WORD WORD2 WORD3 AND MORE WORDS", 5, m);
                        }
                    } else {
                        sendMessage("Couldn't find " + args[0] + "!", 5, m);
                        sendMessage("Use ree!chatprotector add/remove/list", 5, m);
                    }
                } else {
                    if(args[0].equalsIgnoreCase("add")) {
                        if(args.length > 2) {
                            StringBuilder end = new StringBuilder();
                            ArrayList<String> words = new ArrayList<>();
                            for(int i = 2; i < args.length; i++) {
                                words.add(args[i]);
                                end.append("\n").append(args[i]);
                            }
                            de.presti.ree6.addons.impl.ChatProtector.addWordstoProtector(m.getGuild().getId(), words);
                            sendMessage("The Wordlist has been added to your ChatProtector!\nYour Wordlist:\n```" + end + "```", 5, m);
                        } else {
                            de.presti.ree6.addons.impl.ChatProtector.addWordtoProtector(m.getGuild().getId(), args[1]);
                            sendMessage("The Word " + args[1] + " has been added to your ChatProtector!", 5, m);
                        }
                    } else if(args[0].equalsIgnoreCase("remove")) {
                        de.presti.ree6.addons.impl.ChatProtector.removeWordfromProtector(m.getGuild().getId(), args[1]);
                        sendMessage("The Word " + args[1] + " has been removed from your ChatProtector!", 5, m);
                    } else {
                        sendMessage("Couldn't find " + args[0] + "!", 5, m);
                        sendMessage("Use ree!chatprotector add/remove/list", 5, m);
                    }
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!chatprotector add/remove/list", 5, m);
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, m);
        }
        deleteMessage(messageSelf);
    }
}
