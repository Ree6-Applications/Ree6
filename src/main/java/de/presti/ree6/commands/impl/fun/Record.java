package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.audio.AudioPlayerReceiveHandler;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * A command used to record the conversation of a voice channel.
 */
@Command(name = "record", description = "Record the voice channel!", category = Category.FUN)
public class Record implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getGuild().getAudioManager().isConnected()) {
            GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());
            if (guildMusicManager == null || !guildMusicManager.getSendHandler().isMusicPlaying(commandEvent.getGuild())) {
                AudioManager audioManager = commandEvent.getGuild().getAudioManager();

                AudioPlayerReceiveHandler handler = (AudioPlayerReceiveHandler) audioManager.getReceivingHandler();

                if (handler != null) {
                    handler.endReceiving();
                } else {
                    commandEvent.getGuild().getAudioManager().closeAudioConnection();
                }
                Main.getInstance().getCommandManager().sendMessage("Recording stopped!", commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("I am already in a channel!",
                        commandEvent.getChannel(), commandEvent.getInteractionHook());
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
                voiceState.getChannel().getType() == ChannelType.VOICE &&
                voiceState.getChannel() instanceof VoiceChannel voiceChannel) {
            AudioManager audioManager = commandEvent.getGuild().getAudioManager();
            audioManager.openAudioConnection(voiceChannel);

            AudioPlayerReceiveHandler handler = new AudioPlayerReceiveHandler(commandEvent.getMember(), voiceChannel);

            audioManager.setReceivingHandler(handler);

            Main.getInstance().getCommandManager().sendMessage("I am now recording the voice channel!",
                    commandEvent.getChannel(), commandEvent.getInteractionHook());
        } else {
            Main.getInstance().getCommandManager().sendMessage("You are not in a voice channel!",
                    commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
