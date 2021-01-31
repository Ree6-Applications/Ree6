package de.presti.ree6.commands.impl;

import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Unban extends Command {

    public Unban() {
        super("unban", "Unban a User from the Server!");
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {
                if(messageSelf.getMentionedUsers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m);
                    sendMessage("Use ree!unban @user", 5, m);
                } else {
                    sendMessage("User " + messageSelf.getMentionedMembers().get(0).getNickname() + " has been unbanned!", 5, m);
                    m.getGuild().unban(messageSelf.getMentionedUsers().get(0)).queue();
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!unban @user", 5, m);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m);
        }

        messageSelf.delete().queue();
    }
}