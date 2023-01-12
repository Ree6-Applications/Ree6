package de.presti.ree6.streamtools.action.impl;

import de.presti.ree6.main.Main;
import de.presti.ree6.streamtools.action.ActionInfo;
import de.presti.ree6.streamtools.action.IAction;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

@ActionInfo(name = "Say", command = "say", description = "Says a message.", introduced = "2.2.0")
public class SayAction implements IAction {

    @Override
    public void runAction(@NotNull Guild guild, String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return;
        }

        String channelId = arguments[0];

        StringBuilder message = new StringBuilder();

        for (String arg : arguments) {
            message.append(arg).append(" ");
        }

        TextChannel textChannel = guild.getTextChannelById(channelId);

        if (textChannel == null) {
            return;
        }

        Main.getInstance().getCommandManager().sendMessage(message.substring(channelId.length()), textChannel);
    }
}
