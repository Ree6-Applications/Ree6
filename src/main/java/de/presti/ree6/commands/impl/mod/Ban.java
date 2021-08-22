package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Ban extends Command {

    public Ban() {
        super("ban", "Ban the User from the Server!", Category.MOD, new CommandData("ban", "Ban the User from the Server!").addOptions(new OptionData(OptionType.USER, "target", "Which User should be banned.").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {
                if(messageSelf.getMentionedUsers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m);
                    sendMessage("Use ree!ban @user", 5, m);
                } else {
                    sendMessage("User " + messageSelf.getMentionedUsers().get(0).getName() + " has been banned!", 5, m);
                    m.getGuild().ban(messageSelf.getMentionedMembers().get(0), 7).queue();
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!ban @user", 5, m);
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, m);
        }

        deleteMessage(messageSelf);
    }
}
