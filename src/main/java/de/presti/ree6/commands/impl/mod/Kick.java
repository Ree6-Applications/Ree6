package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Kick extends Command {

    public Kick() {
        super("kick", "Kick the User from the Server!", Category.MOD,  new CommandData("kick", "Kick the User from the Server!").addOptions(new OptionData(OptionType.USER, "target", "Which User should be kicked.").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {
                if(messageSelf.getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m, hook);
                    sendMessage("Use " + Main.sqlConnector.getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "kick @user", 5, m, hook);
                } else {
                    if (m.getGuild().getSelfMember().canInteract(messageSelf.getMentionedMembers().get(0)) && sender.canInteract(messageSelf.getMentionedMembers().get(0))) {
                        sendMessage("User " + messageSelf.getMentionedMembers().get(0).getAsMention() + " has been kicked!", 5, m, hook);
                        sender.getGuild().kick(messageSelf.getMentionedMembers().get(0)).queue();
                    } else {
                        sendMessage("Couldn't kick this User because he has a higher Rank then me!", 5, m, hook);
                    }
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m, hook);
                sendMessage("Use " + Main.sqlConnector.getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "kick @user", 5, m, hook);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m, hook);
        }

        deleteMessage(messageSelf, hook);
    }
}
