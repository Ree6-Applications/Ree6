package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class Pause extends Command {

    public Pause() {
        super("pause", "Pause a song!", Category.MUSIC);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).player.setPaused(true);

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription("Song has been paused!");
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
        sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
