package de.presti.ree6.actions.streamtools.action;

import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.main.Main;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

/**
 * StreamAction used to say a message.
 */
@NoArgsConstructor
@ActionInfo(name = "Say", command = "say", description = "Says a message.", introduced = "2.2.0")
public class SayStreamAction implements IStreamAction {

    /**
     * @see IStreamAction#runAction(StreamActionEvent)
     */
    @Override
    public boolean runAction(@NotNull StreamActionEvent event) {
        if (event.getArguments() == null || event.getArguments().length == 0) {
            return false;
        }

        String channelId = event.getArguments()[0];

        StringBuilder message = new StringBuilder();

        for (String arg : event.getArguments()) {
            message.append(arg).append(" ");
        }

        TextChannel textChannel = event.getGuild().getTextChannelById(channelId);

        if (textChannel == null) {
            return false;
        }

        Main.getInstance().getCommandManager().sendMessage(message.substring(channelId.length()), textChannel);
        return true;
    }
}
