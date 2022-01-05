package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class Shuffle extends Command {

    public Shuffle() {
        super("shuffle", "Shuffle your playlist!", Category.MUSIC);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        Main.getInstance().getMusicWorker().getGuildAudioPlayer(
                commandEvent.getGuild()).scheduler.shuffle = !Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.shuffle;

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.shuffle ? "Song Shuffle has been activated!"
                : "Song Shuffle has been deactivated!");
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}