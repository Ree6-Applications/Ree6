package de.presti.ree6.audio;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.utils.data.AudioUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
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
 * JDA threads call all methods in this class when resources are available/ready for processing.
 * The receiver will be provided with the latest 20ms of PCM stereo audio
 * Note you can receive, even while setting yourself to deafen
 */
@Slf4j
public class AudioPlayerReceiveHandler implements AudioReceiveHandler {

    /**
     * Queue of audio to be sent afterward.
     */
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    /**
     * Boolean used to indicate that the handler finished his Job.
     */
    private boolean finished = false;

    /**
     * The ID of the User who wanted to start the recording.
     */
    long creatorId;

    /**
     * The voice channel this handler is currently handling.
     */
    private final AudioChannelUnion audioChannelUnion;

    /**
     * A list with all IDs of users who where in the talk while recording.
     */
    List<String> participants = new ArrayList<>();

    /**
     * The first sent message which should be edited.
     */
    Message message;

    /**
     * Constructor.
     *
     * @param member            The member who started the recording.
     * @param audioChannelUnion The voice channel this handler should handle.
     */
    public AudioPlayerReceiveHandler(Member member, AudioChannelUnion audioChannelUnion) {
        this.creatorId = member.getIdLong();
        this.audioChannelUnion = audioChannelUnion;
        if (audioChannelUnion.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_CHANGE)) {
            LanguageService.getByGuild(member.getGuild(), "label.recording.name").subscribe(name ->
                    LanguageService.getByGuild(member.getGuild(), "message.recording.startReason", member.getUser().getName()).subscribe(reason ->
                            audioChannelUnion.getGuild().getSelfMember().modifyNickname(name).reason(reason).onErrorMap(throwable -> {

                                boolean canTalk = audioChannelUnion.getType() == ChannelType.STAGE ?
                                        audioChannelUnion.asStageChannel().canTalk() :
                                        audioChannelUnion.asVoiceChannel().canTalk();

                                if (canTalk) {
                                    LanguageService.getByGuild(member.getGuild(), "message.default.nameChangeFailed").subscribe(messageContent ->
                                            audioChannelUnion.asGuildMessageChannel().sendMessage(messageContent).queue());
                                }
                                return null;
                            }).queue()));
        }

        message = audioChannelUnion.asGuildMessageChannel().sendMessageEmbeds(new EmbedBuilder()
                .setDescription(LanguageService.getByGuild(member.getGuild(), "message.recording.started").block())
                .setColor(BotConfig.getMainColor())
                .setFooter(LanguageService.getByGuild(member.getGuild(), "label.footerMessage", member.getEffectiveName(), BotConfig.getAdvertisement()).block(), member.getEffectiveAvatarUrl())
                .setTitle(LanguageService.getByGuild(member.getGuild(), "label.recording.start").block())
                .build()).complete();

        if (audioChannelUnion.getGuild().getSelfMember().hasPermission(Permission.VOICE_SET_STATUS) && audioChannelUnion.getType() == ChannelType.VOICE) {
            LanguageService.getByGuild(audioChannelUnion.getGuild(), "label.recording.name").subscribe(name ->
                    audioChannelUnion.asVoiceChannel().modifyStatus(name).queue());

        }
    }

    /**
     * @see AudioReceiveHandler#canReceiveCombined()
     */
    @Override // combine multiple user audio-streams into a single one
    public boolean canReceiveCombined() {
        /* One entry = 20ms of audio, which means 20 * 100 = 2000ms = 2s of audio,
         * but since we want to allow up to 5 minutes of audio, we have to do
         * 20 * 100 * 150 = 300.000ms = 300s = 5 minutes of audio.
         * And since 100 entries are 2s, we would need 15000 entries for 5 minutes of audio.
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

        if (audioChannelUnion.getMembers().size() == 1) {
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

        boolean canTalk = audioChannelUnion.getType() == ChannelType.STAGE ?
                audioChannelUnion.asStageChannel().canTalk() :
                audioChannelUnion.asVoiceChannel().canTalk();

        if (audioChannelUnion.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_CHANGE)) {
            LanguageService.getByGuild(audioChannelUnion.getGuild(), "message.recording.stopReason").subscribe(reason ->
                    audioChannelUnion.getGuild().getSelfMember().modifyNickname(audioChannelUnion.getGuild().getSelfMember().getUser().getName()).reason(reason).onErrorMap(throwable -> {

                        if (canTalk)
                            LanguageService.getByGuild(audioChannelUnion.getGuild(), "message.default.nameChangeFailed")
                                    .subscribe(messageContent -> audioChannelUnion.asGuildMessageChannel().sendMessage(messageContent).queue());
                        return null;
                    }).queue());
        }

        try {
            int queueSize = queue.stream().mapToInt(data -> data.length).sum();
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[queueSize]);
            for (byte[] data : queue) {
                byteBuffer.put(data);
            }

            Recording recording = new Recording(audioChannelUnion.getGuild().getIdLong(), audioChannelUnion.getIdLong(), creatorId, AudioUtil.convertPCMtoWAV(byteBuffer),
                    JsonParser.parseString(new Gson().toJson(participants)).getAsJsonArray());

            SQLSession.getSqlConnector().getSqlWorker().updateEntity(recording).doOnError(x ->
                            audioChannelUnion.asGuildMessageChannel().sendMessage(LanguageService.getByGuild(audioChannelUnion.getGuild(), "message.recording.error", "Upload failed").block()).setFiles(FileUpload.fromData(recording.getRecording(), "recording.wav")))
                    .subscribe(newRecording -> {
                        if (canTalk) {
                            message.editMessageEmbeds(new EmbedBuilder()
                                            .setDescription(LanguageService.getByGuild(audioChannelUnion.getGuild(), "message.recording.stopped").block())
                                            .setColor(BotConfig.getMainColor())
                                            .setFooter(BotConfig.getAdvertisement(), audioChannelUnion.getGuild().getIconUrl())
                                            .setTitle(LanguageService.getByGuild(audioChannelUnion.getGuild(), "label.recording.finished").block())
                                            .build())
                                    .setActionRow(
                                            Button.of(ButtonStyle.LINK, BotConfig.getRecordingUrl() + "?id=" + recording.getIdentifier(),
                                                    LanguageService.getByGuild(audioChannelUnion.getGuild(), "label.download").block(), Emoji.fromCustom("shiba", 941219375535509504L, true)),
                                            Button.primary("r_recordingDownload:" + recording.getIdentifier(), Emoji.fromCustom("sip", 1011956355810209852L, false))
                                                    .withLabel(LanguageService.getByGuild(audioChannelUnion.getGuild(), "label.sendToChat").block()).withDisabled(!BotConfig.allowRecordingInChat())).complete();
                        }
                    });
            // Find a way to still notify that the bot couldn't send the audio.
        } catch (Exception ex) {

            if (canTalk) {
                message.editMessageEmbeds(new EmbedBuilder()
                        .setDescription(LanguageService.getByGuild(audioChannelUnion.getGuild(), "message.recording.error", ex.getMessage()).block())
                        .setColor(Color.RED)
                        .setFooter(BotConfig.getAdvertisement(), audioChannelUnion.getGuild().getIconUrl())
                        .setTitle(LanguageService.getByGuild(audioChannelUnion.getGuild(), "label.error").block())
                        .build()).complete();
            }

            log.error("Something went wrong while converting a recording!", ex);
        }

        if (audioChannelUnion.getGuild().getSelfMember().hasPermission(Permission.VOICE_SET_STATUS) && audioChannelUnion.getType() == ChannelType.VOICE) {
            audioChannelUnion.asVoiceChannel().modifyStatus("").queue();
        }

        audioChannelUnion.getGuild().getAudioManager().closeAudioConnection();
        audioChannelUnion.getGuild().getAudioManager().setReceivingHandler(null);
        queue.clear();
    }
}
