package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;

public class Resume extends Command {
    
    public Resume() {
        super("resume", "Resume a stopped Song!", Category.MUSIC, new String[] { "res", "re" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        EmbedBuilder em = new EmbedBuilder();

        Main.getInstance().getMusicWorker().getGuildAudioPlayer(m.getGuild()).player.setPaused(false);

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription("Song is going to be resumed!");
        em.setFooter(m.getGuild().getName() + " - " + Data.ADVERTISEMENT, m.getGuild().getIconUrl());

        sendMessage(em, 5, m, hook);
    }
}
