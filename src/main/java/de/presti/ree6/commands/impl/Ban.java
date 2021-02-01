package de.presti.ree6.commands.impl;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.xml.soap.Text;

public class Ban extends Command {

    public Ban() {
        super("ban", "Ban the User from the Server!", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {
                if(messageSelf.getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m);
                    sendMessage("Use ree!ban @user", 5, m);
                } else {
                    sendMessage("User " + messageSelf.getMentionedMembers().get(0).getNickname() + " has been banned!", 5, m);
                    m.getGuild().ban(messageSelf.getMentionedMembers().get(0), 12).queue();
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!ban @user", 5, m);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m);
        }

        messageSelf.delete().queue();
    }
}
