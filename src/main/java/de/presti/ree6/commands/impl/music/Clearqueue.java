package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * Clears the Queue.
 */
@Command(name = "clearqueue", description = "command.description.clearQueue", category = Category.MUSIC)
public class Clearqueue implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        EmbedBuilder em = new EmbedBuilder();
        
        em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
        em.setTitle(commandEvent.getResource("label.musicPlayer"));
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(commandEvent.getResource("message.music.clearQueue"));
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl());

        commandEvent.reply(em.build(), 5);
        Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).getScheduler().clearQueue();
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
        return new String[] { "clearq", "cq" };
    }
}