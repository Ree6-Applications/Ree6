package de.presti.ree6.streamtools.action.impl;

import de.presti.ree6.main.Main;
import de.presti.ree6.streamtools.action.StreamActionInfo;
import de.presti.ree6.streamtools.action.IStreamAction;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

/**
 * StreamAction used to leave the current connected Voicechannel.
 */
@NoArgsConstructor
@StreamActionInfo(name = "VoiceLeave", command = "voice-leave", description = "Leaves the current connected Voicechannel.", introduced = "2.2.0")
public class VoiceLeaveStreamAction implements IStreamAction {

    /**
     * @inheritDoc
     */
    @Override
    public void runAction(@NotNull Guild guild, String[] arguments) {
        Main.getInstance().getMusicWorker().disconnect(guild);
    }

}
