package de.presti.ree6.actions.streamtools.action;

import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.main.Main;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * StreamAction used to leave the current connected Voicechannel.
 */
@NoArgsConstructor
@ActionInfo(name = "VoiceLeave", command = "voice-leave", description = "Leaves the current connected Voicechannel.", introduced = "2.2.0")
public class VoiceLeaveStreamAction implements IStreamAction {

    /**
     * @see IStreamAction#runAction(StreamActionEvent)
     */
    @Override
    public boolean runAction(@NotNull StreamActionEvent event) {
        if (!Main.getInstance().getMusicWorker().isConnectedMember(event.getGuild().getSelfMember())) return false;

        Main.getInstance().getMusicWorker().disconnect(event.getGuild());
        return true;
    }

}
