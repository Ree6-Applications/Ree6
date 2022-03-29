package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

@Command(name = "stop", description = "Stop the current playing Song.", category = Category.MUSIC)
public class Stop implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()) != null) {
            if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent) && Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
                return;
            }
            Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.stopAll(commandEvent.getGuild(), commandEvent.getInteractionHook());
        } else {
            Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                            commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl()).setTitle("Music Player!")
                    .setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription("Im not playing any Music!"), 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
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