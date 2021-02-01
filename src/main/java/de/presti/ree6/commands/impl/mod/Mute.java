package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class Mute extends Command {

    public Mute() {
        super("mute", "Mute a User on the Server!", Category.MUSIC);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {

                if(!Main.sqlWorker.hasMuteSetuped(m.getGuild().getId())) {
                    sendMessage("Mute Role hasnt been setuped!\nTo setup it up type ree!setup mute @MuteRole !", 5, m);
                    return;
                }

                if(messageSelf.getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m);
                    sendMessage("Use ree!mute @user", 5, m);
                } else {
                    sendMessage("User " + messageSelf.getMentionedMembers().get(0).getNickname() + " has been muted!", 5, m);
                    Role r = m.getGuild().getRoleById(Main.sqlWorker.getMuteRoleID(sender.getGuild().getId()));
                    m.getGuild().addRoleToMember(messageSelf.getMentionedMembers().get(0), r).queue();
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!mute @user", 5, m);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m);
        }
        messageSelf.delete().queue();
    }
}
