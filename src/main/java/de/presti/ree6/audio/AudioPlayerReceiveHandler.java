package de.presti.ree6.audio;

import de.presti.ree6.utils.data.AudioUtil;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
    All methods in this class are called by JDA threads when resources are available/ready for processing.
    The receiver will be provided with the latest 20ms of PCM stereo audio
    Note you can receive even while setting yourself to deafened
 */
public class AudioPlayerReceiveHandler implements AudioReceiveHandler {

    /**
     * Queue of audio to be sent afterwards.
     */
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    /**
     * Boolean used to indicated that handler finished his Job.
     */
    private boolean finished = false;

    /**
     * The voice channel this handler is currently handling.
     */
    private final VoiceChannel voiceChannel;

    /**
     * Constructor.
     * @param voiceChannel The voice channel this handler should handle.
     */
    public AudioPlayerReceiveHandler(VoiceChannel voiceChannel) {
        this.voiceChannel = voiceChannel;
    }

    /**
     * @see AudioReceiveHandler#canReceiveCombined()
     */
    @Override // combine multiple user audio-streams into a single one
    public boolean canReceiveCombined()
    {
        /* one entry = 20ms of audio, which means 20 * 100 = 2000ms = 2s of audio,
         * but since we want to allow up to 5 minute of audio we have to do
         * 20 * 100 * 150 =  300.000ms = 5 minutes of audio.
         * And since 100 entries are 2s we would need 15000 entries for 5 minutes of audio.
         */
        return queue.size() < 15000;
    }

    /**
     * @see AudioReceiveHandler#canReceiveUser()
     */
    @Override
    public boolean includeUserInCombinedAudio(@NotNull User user) {
        return !user.isBot();
    }

    /**
     * @see AudioReceiveHandler#handleCombinedAudio(CombinedAudio)
     */
    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio)
    {
        if (combinedAudio.getUsers().isEmpty()) {
            if (voiceChannel.getMembers().size() == 1) {
                endReceiving();
            }
            return;
        }

        if (voiceChannel.getMembers().size() == 1) {
            endReceiving();
            return;
        }

        byte[] data = combinedAudio.getAudioData(1.0f);
        queue.add(data);

        if (!canReceiveCombined()) {
            endReceiving();
        }
    }

    /**
     * Method called when the recording should stop.
     */
    private void endReceiving()
    {
        if (finished) {
            return;
        }

        finished = true;

        byte[] rawData = new byte[0];
        for (byte[] data : queue)
        {
            rawData = ArrayUtils.addAll(rawData, data);
        }

        try {
            byte[] data = AudioUtil.rawToWave(rawData);
            voiceChannel.sendMessage("Here is your audio!").addFile(data, "audio.wav").queue();
        } catch (Exception ex) {
            voiceChannel.sendMessage("Something went wrong while converting your audio!").queue();
        }

        voiceChannel.getGuild().getAudioManager().closeAudioConnection();
        queue.clear();
    }
}
