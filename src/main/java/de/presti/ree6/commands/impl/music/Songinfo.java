package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class Songinfo extends CommandClass {

    public Songinfo() {
        super("songinfo", "Get the currently playing Track!", Category.MUSIC, new String[] { "trackinfo", "cq" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        EmbedBuilder em = new EmbedBuilder();

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).player.getPlayingTrack() == null ? "No Song is being played right now!" : "**Song:** ```"
                + Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).player.getPlayingTrack().getInfo().title + " by " + Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).player.getPlayingTrack().getInfo().author + "```");
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
