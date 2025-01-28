package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.audio.AudioPlayerReceiveHandler;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A command used to record the conversation of a voice channel.
 */
@Command(name = "record", description = "command.description.record", category = Category.FUN, allowAppInstall = false)
public class Record implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getGuild().getAudioManager().isConnected()) {
            GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());
            if (guildMusicManager == null || !guildMusicManager.isMusicPlaying()) {
                AudioManager audioManager = commandEvent.getGuild().getAudioManager();

                AudioPlayerReceiveHandler handler = (AudioPlayerReceiveHandler) audioManager.getReceivingHandler();

                if (handler != null) {
                    handler.endReceiving();
                } else {
                    if (audioManager.getGuild().getSelfMember().hasPermission(Permission.VOICE_SET_STATUS) &&
                            audioManager.isConnected() &&
                            audioManager.getConnectedChannel().getType() == ChannelType.VOICE) {
                        audioManager.getConnectedChannel().asVoiceChannel().modifyStatus("").queue();
                    }
                    audioManager.closeAudioConnection();
                }

                commandEvent.reply(commandEvent.getResource("message.record.recordingStopped"));
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.alreadyInVoiceChannel"));
            }
        } else {
            connectAndRecord(commandEvent);
        }
    }

    /**
     * Start the recording of the voice channel.
     * @param commandEvent the command event.
     */
    public void connectAndRecord(CommandEvent commandEvent) {
        GuildVoiceState voiceState = commandEvent.getMember().getVoiceState();
        if (voiceState != null &&
                voiceState.inAudioChannel() &&
                voiceState.getChannel() != null &&
                voiceState.getChannel().getType().isAudio()) {

            AudioChannelUnion audioChannelUnion = voiceState.getChannel();

            AudioManager audioManager = commandEvent.getGuild().getAudioManager();

            AudioPlayerReceiveHandler handler = new AudioPlayerReceiveHandler(commandEvent.getMember(), audioChannelUnion);

            audioManager.setReceivingHandler(handler);

            audioManager.openAudioConnection(audioChannelUnion);

            if (commandEvent.getGuild().getSelfMember().getVoiceState() != null &&
                    commandEvent.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                commandEvent.getGuild().getSelfMember().deafen(false).queue();
            }

            commandEvent.reply(commandEvent.getResource("message.record.recordingStarted"));
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.notInVoiceChannel"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("record", "command.description.record");
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
