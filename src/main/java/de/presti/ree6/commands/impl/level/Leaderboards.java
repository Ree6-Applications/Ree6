package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;

public class Leaderboards extends Command {

    public Leaderboards() {
        super("leaderboard", "Shows you the Rank Leaderboard", Category.LEVEL, new String[]{ "lb" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        EmbedBuilder em = new EmbedBuilder();

        em.setThumbnail(sender.getUser().getAvatarUrl());
        em.setTitle("Leaderboard");

        ArrayList<String> top = Main.sqlWorker.getTop(3, m.getGuild().getId());

        em.addField("2", "**<@" + top.get(1) +">**\n**Level:**" + getLevel(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(1))) +"\n**XP:** " + Main.sqlWorker.getXP(m.getGuild().getId(), top.get(1)),true);


        em.addField("1", "**<@" + top.get(0) +">**\n**Level:**" + getLevel(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(0))) +"\n**XP:** " + Main.sqlWorker.getXP(m.getGuild().getId(), top.get(0)),true);


        em.addField("3", "**<@" + top.get(2) +">**\n**Level:**" + getLevel(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(2))) +"\n**XP:** " + Main.sqlWorker.getXP(m.getGuild().getId(), top.get(2)),true);


        em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());
    }

    public int getLevel(int xp) {
        int level = 1;

        while (xp > 1000) {
            xp -= 1000;
            level++;
        }

        return level;
    }
}
