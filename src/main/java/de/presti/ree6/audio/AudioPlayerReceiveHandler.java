package de.presti.ree6.audio;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.utils.data.AudioUtil;
import de.presti.ree6.bot.BotConfig;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * All methods in this class are called by JDA threads when resources are available/ready for processing.
 * The receiver will be provided with the latest 20ms of PCM stereo audio
 * Note you can receive even while setting yourself to deafened
 */
@Slf4j
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
    private final AudioChannelUnion voiceChannel;

    /**
     * A list with all IDs of users who where in the talk while recording.
     */
    List<String> participants = new ArrayList<>();

    /**
     * The first send message which should be edited.
     */
    Message message;

    /**
     * Constructor.
     *
     * @param member       The member who started the recording.
     * @param voiceChannel The voice channel this handler should handle.
     */
    public AudioPlayerReceiveHandler(Member member, AudioChannelUnion voiceChannel) {
        this.creatorId = member.getId();
        this.voiceChannel = voiceChannel;
        if (voiceChannel.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_CHANGE)) {
            voiceChannel.getGuild().getSelfMember().modifyNickname(LanguageService.getByGuild(member.getGuild(), "label.recording.name")).reason(LanguageService.getByGuild(member.getGuild(), "message.recording.startReason", member.getUser().getName())).onErrorMap(throwable -> {
                if (voiceChannel instanceof VoiceChannel)
                    if (voiceChannel.asVoiceChannel().canTalk()) voiceChannel.asVoiceChannel().sendMessage(LanguageService.getByGuild(member.getGuild(), "message.default.nameChangeFailed")).queue();
                else
                    if (voiceChannel.asStageChannel().canTalk()) voiceChannel.asStageChannel().sendMessage(LanguageService.getByGuild(member.getGuild(), "message.default.nameChangeFailed")).queue();
                return null;
            }).queue();
        }
        if (voiceChannel instanceof VoiceChannel)
            message = voiceChannel.asVoiceChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setDescription(LanguageService.getByGuild(member.getGuild(), "message.recording.started"))
                    .setColor(Color.YELLOW)
                    .setFooter("Requested by " + member.getEffectiveName() + " - " + BotConfig.getAdvertisement(), member.getEffectiveAvatarUrl())
                    .setTitle(LanguageService.getByGuild(member.getGuild(), "label.recording.start"))
                    .build()).complete();
        else
            message = voiceChannel.asStageChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setDescription(LanguageService.getByGuild(member.getGuild(), "message.recording.started"))
                    .setColor(Color.YELLOW)
                    .setFooter("Requested by " + member.getEffectiveName() + " - " + BotConfig.getAdvertisement(), member.getEffectiveAvatarUrl())
                    .setTitle(LanguageService.getByGuild(member.getGuild(), "label.recording.start"))
                    .build()).complete();
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
            voiceChannel.getGuild().getSelfMember().modifyNickname(voiceChannel.getGuild().getSelfMember().getUser().getName()).reason(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.stopReason")).onErrorMap(throwable -> {
                if (voiceChannel instanceof VoiceChannel)
                    if (voiceChannel.asVoiceChannel().canTalk()) voiceChannel.asVoiceChannel().sendMessage(LanguageService.getByGuild(voiceChannel.getGuild(), "message.default.nameChangeFailed")).queue();
                else
                    if (voiceChannel.asStageChannel().canTalk()) voiceChannel.asStageChannel().sendMessage(LanguageService.getByGuild(voiceChannel.getGuild(), "message.default.nameChangeFailed")).queue();
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

            boolean failedToUpload = false;

            try {
                SQLSession.getSqlConnector().getSqlWorker().updateEntity(recording);
            } catch (Exception ignore) {
                failedToUpload = true;
            }
            if (voiceChannel instanceof VoiceChannel) {
                message.editMessageEmbeds(new EmbedBuilder()
                                .setDescription(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.stopped"))
                                .setColor(Color.GREEN)
                                .setFooter(BotConfig.getAdvertisement(), voiceChannel.getGuild().getIconUrl())
                                .setTitle(LanguageService.getByGuild(voiceChannel.getGuild(), "label.recording.finished"))
                                .build())
                        .setActionRow(
                                new ButtonImpl("ree6RedirectButton", LanguageService.getByGuild(voiceChannel.getGuild(), "label.download"), ButtonStyle.LINK,
                                        BotConfig.getRecordingUrl() + "?id=" + recording.getIdentifier(), failedToUpload, Emoji.fromCustom("shiba", 941219375535509504L, true)),
                                Button.primary("r_recordingDownload:" + recording.getIdentifier(), Emoji.fromCustom("sip", 1011956355810209852L, false))
                                        .withLabel(LanguageService.getByGuild(voiceChannel.getGuild(), "label.sendToChat")).withDisabled(!BotConfig.allowRecordingInChat() || failedToUpload)).complete();

                if (failedToUpload) {
                    voiceChannel.asVoiceChannel().sendMessage(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.error", "Upload failed")).setFiles(FileUpload.fromData(recording.getRecording(), "recording.wav"));
                }
            }
            else {
                message.editMessageEmbeds(new EmbedBuilder()
                                .setDescription(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.stopped"))
                                .setColor(Color.GREEN)
                                .setFooter(BotConfig.getAdvertisement(), voiceChannel.getGuild().getIconUrl())
                                .setTitle(LanguageService.getByGuild(voiceChannel.getGuild(), "label.recording.finished"))
                                .build())
                        .setActionRow(
                                new ButtonImpl("ree6RedirectButton", LanguageService.getByGuild(voiceChannel.getGuild(), "label.download"), ButtonStyle.LINK,
                                        BotConfig.getRecordingUrl() + "?id=" + recording.getIdentifier(), failedToUpload, Emoji.fromCustom("shiba", 941219375535509504L, true)),
                                Button.primary("r_recordingDownload:" + recording.getIdentifier(), Emoji.fromCustom("sip", 1011956355810209852L, false))
                                        .withLabel(LanguageService.getByGuild(voiceChannel.getGuild(), "label.sendToChat")).withDisabled(!BotConfig.allowRecordingInChat() || failedToUpload)).complete();

                if (failedToUpload) {
                    voiceChannel.asStageChannel().sendMessage(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.error", "Upload failed")).setFiles(FileUpload.fromData(recording.getRecording(), "recording.wav"));
                }
            }
            // Find a way to still notify that the bot couldn't send the audio.
        } catch (Exception ex) {
            if (voiceChannel instanceof VoiceChannel)
                if (voiceChannel.asVoiceChannel().canTalk()) {
                    message.editMessageEmbeds(new EmbedBuilder()
                            .setDescription(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.error", ex.getMessage()))
                            .setColor(Color.RED)
                            .setFooter(BotConfig.getAdvertisement(), voiceChannel.getGuild().getIconUrl())
                            .setTitle(LanguageService.getByGuild(voiceChannel.getGuild(), "label.error"))
                            .build()).complete();
                }
            else
                if (voiceChannel.asStageChannel().canTalk()) {
                    message.editMessageEmbeds(new EmbedBuilder()
                            .setDescription(LanguageService.getByGuild(voiceChannel.getGuild(), "message.recording.error", ex.getMessage()))
                            .setColor(Color.RED)
                            .setFooter(BotConfig.getAdvertisement(), voiceChannel.getGuild().getIconUrl())
                            .setTitle(LanguageService.getByGuild(voiceChannel.getGuild(), "label.error"))
                            .build()).complete();
                }

            log.error("Something went wrong while converting a recording!", ex);
        }

        voiceChannel.getGuild().getAudioManager().closeAudioConnection();
        queue.clear();
    }
}
