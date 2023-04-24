package de.presti.ree6.streamtools.action.impl;

import com.github.twitch4j.common.events.TwitchEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.streamtools.action.StreamActionInfo;
import de.presti.ree6.streamtools.action.IStreamAction;
import io.sentry.Sentry;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

/**
 * StreamAction used to join a voice channel.
 */
@NoArgsConstructor
@StreamActionInfo(name = "VoiceJoin", command = "voice-join", description = "Joins a voice channel.", introduced = "2.2.0")
public class VoiceJoinStreamAction implements IStreamAction {

    /**
     * @inheritDoc
     */
    @Override
    public boolean runAction(@NotNull Guild guild, TwitchEvent twitchEvent, String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return false;
        }

        try {
            VoiceChannel voiceChannel = guild.getVoiceChannelById(arguments[0]);
            if (voiceChannel == null) return false;

            if (Main.getInstance().getMusicWorker().isConnectedMember(guild.getSelfMember())) {
                if (guild.getSelfMember().getVoiceState().getChannel().getIdLong() == voiceChannel.getIdLong())
                    return true;
                guild.moveVoiceMember(guild.getSelfMember(), voiceChannel).queue();
            } else {
                Main.getInstance().getMusicWorker().connectToAudioChannel(guild.getAudioManager(), voiceChannel);
            }

            return true;
        } catch (Exception exception) {
            Sentry.captureMessage("Invalid Voice Channel ID! Related guild: " + guild.getIdLong());
        }

        return false;
    }
}
