package de.presti.ree6.streamtools.action.impl;

import de.presti.ree6.main.Main;
import de.presti.ree6.streamtools.action.ActionInfo;
import de.presti.ree6.streamtools.action.IAction;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

@ActionInfo(name = "VoiceLeave", command = "voice-leave", description = "Leaves the current connected Voicechannel.", introduced = "2.2.0")
public class VoiceLeaveAction implements IAction {

    /**
     * @inheritDoc
     */
    @Override
    public void runAction(@NotNull Guild guild, String[] arguments) {
        Main.getInstance().getMusicWorker().disconnect(guild);
    }

}
