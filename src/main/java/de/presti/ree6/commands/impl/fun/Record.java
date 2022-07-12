package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.audio.AudioPlayerReceiveHandler;
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

@Command(name = "record", description = "Record the voice channel!", category = Category.FUN)
public class Record implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getGuild().getAudioManager().isConnected()) {
            Main.getInstance().getCommandManager().sendMessage("I am already in a channel!",
                    commandEvent.getChannel(), commandEvent.getInteractionHook());
        } else {
            GuildVoiceState voiceState = commandEvent.getMember().getVoiceState();
            if (voiceState != null &&
                    voiceState.inAudioChannel() &&
                    voiceState.getChannel().getType() == ChannelType.VOICE &&
                    voiceState.getChannel() instanceof VoiceChannel voiceChannel) {

                AudioManager audioManager = commandEvent.getGuild().getAudioManager();
                audioManager.openAudioConnection(voiceChannel);

                AudioPlayerReceiveHandler handler = new AudioPlayerReceiveHandler(voiceChannel);

                audioManager.setReceivingHandler(handler);

                Main.getInstance().getCommandManager().sendMessage("I am now recording the voice channel!",
                        commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("You are not in a voice channel!",
                        commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
