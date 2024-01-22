package de.presti.ree6.actions.streamtools.action;

import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.main.Main;
import io.sentry.Sentry;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

/**
 * StreamAction used to join a voice channel.
 */
@NoArgsConstructor
@ActionInfo(name = "VoiceJoin", command = "voice-join", description = "Joins a voice channel.", introduced = "2.2.0")
public class VoiceJoinStreamAction implements IStreamAction {

    /**
     * @see IStreamAction#runAction(StreamActionEvent)
     */
    @Override
    public boolean runAction(@NotNull StreamActionEvent event) {
        if (event.getArguments() == null || event.getArguments().length < 1) {
            return false;
        }

        try {
            VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getArguments()[0]);
            if (voiceChannel == null) return false;

            if (Main.getInstance().getMusicWorker().isConnectedMember(event.getGuild().getSelfMember())) {
                if (event.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong() == voiceChannel.getIdLong())
                    return true;
                event.getGuild().moveVoiceMember(event.getGuild().getSelfMember(), voiceChannel).complete();
            } else {
                Main.getInstance().getMusicWorker().connectToAudioChannel(event.getGuild().getAudioManager(), voiceChannel);
            }

            return true;
        } catch (Exception exception) {
            Sentry.captureMessage("Invalid Voice Channel ID! Related guild: " + event.getGuild().getIdLong());
        }

        return false;
    }
}
