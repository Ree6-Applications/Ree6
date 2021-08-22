package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
        ArrayList<String> topvc = Main.sqlWorker.getTopVC(3, m.getGuild().getId());

        if(!top.isEmpty() && !topvc.isEmpty()) {

            em.addField("**Chat Leaderboard**", "", true);
            em.addBlankField(true);
            em.addField("**Voice Leaderboard**", "", true);

            em.addField("1", "**<@" + top.get(0) + ">**\n**ChatLevel: **" + getLevel(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(0))) + "\n**ChatXP:** " + getFormattedXP(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(0))), true);
            em.addBlankField(true);
            em.addField("1", "**<@" + topvc.get(0) + ">**\n**VoiceLevel: **" + getLevelVC(Main.sqlWorker.getXPVC(m.getGuild().getId(), top.get(0))) + "\n**VoiceXP:** " + getFormattedXP(Main.sqlWorker.getXPVC(m.getGuild().getId(), top.get(0))), true);

            em.addField("2", "**<@" + top.get(1) + ">**\n**Level: **" + getLevel(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(1))) + "\n**XP:** " + getFormattedXP(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(1))), true);
            em.addBlankField(true);
            em.addField("2", "**<@" + topvc.get(1) + ">**\n**VoiceLevel: **" + getLevelVC(Main.sqlWorker.getXPVC(m.getGuild().getId(), top.get(1))) + "\n**VoiceXP:** " + getFormattedXP(Main.sqlWorker.getXPVC(m.getGuild().getId(), top.get(1))), true);

            em.addField("3", "**<@" + top.get(2) + ">**\n**Level: **" + getLevel(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(2))) + "\n**XP:** " + getFormattedXP(Main.sqlWorker.getXP(m.getGuild().getId(), top.get(2))), true);
            em.addBlankField(true);
            em.addField("3", "**<@" + topvc.get(2) + ">**\n**VoiceLevel: **" + getLevelVC(Main.sqlWorker.getXPVC(m.getGuild().getId(), top.get(2))) + "\n**VoiceXP:** " + getFormattedXP(Main.sqlWorker.getXPVC(m.getGuild().getId(), top.get(2))), true);

        } else {
            em.addField("Error", "There arent any Top 3s", true);
        }

        em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());
        sendMessage(em, m);
    }

    public int getLevel(long xp) {
        int level = 1;

        while (xp > 1000) {
            xp -= 1000;
            level++;
        }

        return level;
    }

    public int getLevelVC(long xp) {
        int level = 1;

        while (xp > 1000) {
            xp -= 1000;
            level++;
        }

        return level;
    }

    public String getFormattedXP(long xp) {
        String end = "";

        if(xp >= 1000000000000L) {
            end = ((xp / 1000000000000L) + "").replaceAll("l", "") + "mil";
        } else if(xp >= 1000000000) {
            end = ((xp / 1000000000) + "").replaceAll("l", "") + "mil";
        } else if(xp >= 1000000) {
            end = ((xp / 1000000) + "").replaceAll("l", "") + "mio";
        } else if(xp >= 1000) {
            end = ((xp / 1000) + "").replaceAll("l", "") + "k";
        } else {
            end = "" + xp;
        }

        return end;
    }
}
