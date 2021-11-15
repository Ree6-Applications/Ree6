package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Mute extends Command {

    public Mute() {
        super("mute", "Mute a User on the Server!", Category.MOD, new CommandData("mute", "Mute a User on the Server!").addOptions(new OptionData(OptionType.USER, "target", "Which User should be muted.").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {

                if(!Main.sqlWorker.hasMuteSetuped(m.getGuild().getId())) {
                    sendMessage("Mute Role hasn't been setuped!\nTo setup it up type " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "setup mute @MuteRole !", 5, m, hook);
                    return;
                }

                if(messageSelf.getMentionedUsers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m, hook);
                    sendMessage("Use " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "mute @user", 5, m, hook);
                } else {
                    if (m.getGuild().getSelfMember().canInteract(messageSelf.getMentionedMembers().get(0)) && sender.canInteract(messageSelf.getMentionedMembers().get(0))) {
                        sendMessage("User " + messageSelf.getMentionedMembers().get(0).getAsMention() + " has been muted!", 5, m, hook);
                        Role r = m.getGuild().getRoleById(Main.sqlWorker.getMuteRoleID(sender.getGuild().getId()));
                        m.getGuild().addRoleToMember(messageSelf.getMentionedMembers().get(0), r).queue();
                    } else {
                        sendMessage("Couldn't mute this User because he has a higher Rank then me!", 5, m, hook);
                    }
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m, hook);
                sendMessage("Use " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "mute @user", 5, m, hook);
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, m, hook);
        }
        deleteMessage(messageSelf);
    }
}
