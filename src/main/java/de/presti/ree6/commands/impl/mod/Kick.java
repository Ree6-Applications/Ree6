package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;

public class Kick extends Command {

    public Kick() {
        super("kick", "Kick the User from the Server!", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {
                if(messageSelf.getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m);
                    sendMessage("Use ree!kick @user", 5, m);
                } else {
                    sendMessage("User " + messageSelf.getMentionedMembers().get(0).getNickname() + " has been kicked!", 5, m);
                    sender.getGuild().kick(messageSelf.getMentionedMembers().get(0)).queue();
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!kick @user", 5, m);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m);
        }
        messageSelf.delete().queue();
    }
}
