package de.presti.ree6.audio;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.utils.data.AudioUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
     * The ID of the User who wanted to start the recording.
     */
    String creatorId;

    /**
     * The voice channel this handler is currently handling.
     */
    private final VoiceChannel voiceChannel;

    /**
     * A list with all IDs of users who where in the talk while recording.
     */
    List<String> participants = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param voiceChannel The voice channel this handler should handle.
     */
    public AudioPlayerReceiveHandler(Member member, VoiceChannel voiceChannel) {
        this.creatorId = member.getId();
        this.voiceChannel = voiceChannel;
        if (voiceChannel.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_CHANGE)) {
            voiceChannel.getGuild().getSelfMember().modifyNickname("[\uD83D\uDD34] Recording!").reason("Recording started.").onErrorMap(throwable -> {
                voiceChannel.sendMessage("Could not change nickname.").queue();
                return null;
            }).queue();
        }
        voiceChannel.sendMessage("I am now recording this Voice-channel as requested by an participant!").queue();
    }

    /**
     * @see AudioReceiveHandler#canReceiveCombined()
     */
    @Override // combine multiple user audio-streams into a single one
    public boolean canReceiveCombined() {
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
    public void handleCombinedAudio(@NotNull CombinedAudio combinedAudio) {
        if (finished) {
            return;
        }

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

        HashSet<String> hashSet = new HashSet<>(participants);
        combinedAudio.getUsers().stream().map(User::getId).filter(s -> !hashSet.contains(s)).forEach(s -> participants.add(s));

        byte[] data = combinedAudio.getAudioData(1.0f);
        queue.add(data);

        if (!canReceiveCombined()) {
            endReceiving();
        }
    }

    /**
     * Method called when the recording should stop.
     */
    public void endReceiving() {
        if (finished) {
            return;
        }

        finished = true;

        if (voiceChannel.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_CHANGE)) {
            voiceChannel.getGuild().getSelfMember().modifyNickname(voiceChannel.getGuild().getSelfMember().getUser().getName()).reason("Recording finished.").onErrorMap(throwable -> {
                voiceChannel.sendMessage("Could not change nickname.").queue();
                return null;
            }).queue();
        }

        try {
            int queueSize = queue.stream().mapToInt(data -> data.length).sum();
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[queueSize]);
            for (byte[] data : queue) {
                byteBuffer.put(data);
            }

            Recording recording = new Recording(voiceChannel.getGuild().getId(), voiceChannel.getId(), creatorId, AudioUtil.convertPCMtoWAV(byteBuffer),
                    JsonParser.parseString(new Gson().toJson(participants)).getAsJsonArray());

            Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(recording);

            if (voiceChannel.canTalk()) {
                voiceChannel.sendMessage("Your Audio has been converted and is now available for download!").queue();
            }
            // Find a way to still notify that the bot couldn't send the audio.
        } catch (Exception ex) {
            if (voiceChannel.canTalk()) {
                voiceChannel.sendMessage("Something went wrong while converting your audio!\nReason: " + ex.getMessage()).queue();
            }

            Main.getInstance().getLogger().error("Something went wrong while converting a recording!", ex);
        }

        voiceChannel.getGuild().getAudioManager().closeAudioConnection();
        queue.clear();
    }
}
