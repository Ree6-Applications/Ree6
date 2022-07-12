package de.presti.ree6.audio;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.commons.lang.ArrayUtils;

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
     * @see AudioReceiveHandler#handleCombinedAudio(CombinedAudio)
     */
    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio)
    {
        if (combinedAudio.getUsers().isEmpty()) {
            endReceiving();
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
        byte[] rawData = new byte[0];
        for (byte[] data : queue)
        {
            rawData = ArrayUtils.addAll(rawData, data);
        }
        voiceChannel.sendMessage("Here is your audio!").addFile(rawData, "audio.wav").queue();
        voiceChannel.getGuild().getAudioManager().closeAudioConnection();
        queue.clear();
    }
}
