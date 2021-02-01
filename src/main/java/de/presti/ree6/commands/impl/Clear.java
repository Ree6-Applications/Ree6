package de.presti.ree6.commands.impl;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Clear extends Command {

    public Clear() {
        super("clear", "Clear the Chat!", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if(sender.hasPermission(Permission.ADMINISTRATOR)) {
            if(args.length == 1) {
                try {
                    if (Integer.parseInt(args[0]) <= 100 && Integer.parseInt(args[0]) >= 2) {
                        messageSelf.delete().queue();
                        List<Message> messages = m.getHistory().retrievePast(Integer.parseInt(args[0])).complete();
                        try {
                            m.deleteMessages(messages).queue();

                            sendMessage(messages.size() + " has been deleted!", 5, m);

                        } catch (Exception ex) {
                            if(ex instanceof IllegalArgumentException) {
                                sendMessage("" + (ex.toString().toLowerCase().startsWith("java.lang.illegalargumentexception: must provide at") ? "Given Paramater is below 100 and 2!" : "Error while deleting:" + ex.toString().split(":")[1]), 5, m);
                            } else {
                                sendMessage("Error while deleting:" + ex.toString().split(":")[1], 5, m);
                            }
                        }

                    } else {
                        sendMessage(args[0] + " isn't between 2 and 100 !", 5, m);
                        sendMessage("Use ree!clear 1-100", 5, m);
                    }
                } catch (Exception ex) {
                    sendMessage(args[0] + " isn't a number!", 5, m);
                    sendMessage("Use ree!clear 2-100", 5, m);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!clear 2-100", 5, m);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m);
        }
    }
}
