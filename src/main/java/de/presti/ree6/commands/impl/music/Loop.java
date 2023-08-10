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
 * Set the Song onto Loop.
 */
@Command(name = "loop", description = "command.description.loop", category = Category.MUSIC)
public class Loop implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            commandEvent.reply(commandEvent.getResource("message.music.notConnected"));
            return;
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        EmbedBuilder em = new EmbedBuilder();

        em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
        em.setTitle(commandEvent.getResource("label.musicPlayer"));
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).getScheduler().loop() ?
                commandEvent.getResource("message.music.loop.enabled") :
                commandEvent.getResource("message.music.loop.disabled"));
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl());

        commandEvent.reply(em.build(), 5);
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