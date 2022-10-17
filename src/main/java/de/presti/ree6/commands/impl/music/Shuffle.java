package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.Color;

/**
 * Shuffle the current Playlist.
 */
@Command(name = "shuffle", description = "command.description.shuffle", category = Category.MUSIC)
public class Shuffle implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            Main.getInstance().getCommandManager().sendMessage("Im not connected to any Channel, so there is nothing to shuffle!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        EmbedBuilder em = new EmbedBuilder();

        Main.getInstance().getMusicWorker().getGuildAudioPlayer(
                commandEvent.getGuild()).getScheduler().shuffle();

        em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription("The current Song-Queue has been shuffled!");
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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