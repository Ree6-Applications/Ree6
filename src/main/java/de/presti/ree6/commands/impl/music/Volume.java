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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;

public class Volume extends Command {

    public Volume() {
        super("volume", "Set the Volume!", Category.MUSIC, new CommandData("volume", "Set the Volume!").addOptions(new OptionData(OptionType.INTEGER, "amount", "The Volume that the Ree6 Music Player should be!").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {

        EmbedBuilder em = new EmbedBuilder();

        if (args.length == 1) {
            int vol;

            try {
                vol = Integer.parseInt(args[0]);
            } catch (Exception e) {
                vol = 50;
            }

            Main.musikWorker.getGuildAudioPlayer(m.getGuild()).player.setVolume(vol);

            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("The Volume has been set to " + vol);

        } else {
            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("Type ree!volume [voulume]");
        }
        em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());

        sendMessage(em, 5, m, hook);
    }
}