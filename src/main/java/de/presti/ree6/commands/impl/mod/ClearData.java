package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.Duration;
import java.util.ArrayList;

@Command(name = "cleardata", category = Category.MOD, description = "Clear currently stored Invite logs.")
public class ClearData implements ICommand {

    ArrayList<String> timeout = new ArrayList<>();

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (!timeout.contains(commandEvent.getGuild().getId())) {
                Main.getInstance().getSqlConnector().getSqlWorker().clearInvites(commandEvent.getGuild().getId());
                if (commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER))
                    commandEvent.getGuild().retrieveInvites().queue(invites -> invites.stream().filter(invite -> invite.getInviter() != null).forEach(invite -> Main.getInstance().getSqlConnector().getSqlWorker().setInvite(commandEvent.getGuild().getId(), invite.getInviter().getId(), invite.getCode(), invite.getUses())));
                Main.getInstance().getCommandManager().sendMessage("All stored Invites have been cleared, and replaced.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                new Thread(() -> {
                    try {
                        Thread.sleep(Duration.ofMinutes(10).toMillis());
                    } catch (Exception ignored) {}
                    timeout.remove(commandEvent.getGuild().getId());
                }).start();
            } else {
                Main.getInstance().getCommandManager().sendMessage("You already used this Command in the last 10 Minutes, please wait until you use it again.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
