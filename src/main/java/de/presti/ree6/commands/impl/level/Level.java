package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Level extends Command {

    public Level() {
        super("level", "Shows the Level of a User!", Category.LEVEL, new String[] {"lvl", "xp", "rank"});
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
            if (args.length <= 1) {
                if(messageSelf.getMentionedMembers().isEmpty()) {

                    EmbedBuilder em = new EmbedBuilder();

                    em.setThumbnail(sender.getUser().getAvatarUrl());
                    em.setTitle("Level");

                    long chatxp = Main.sqlWorker.getXP(m.getGuild().getId(), sender.getUser().getId());

                    int chatlevel = 1;

                    while (chatxp > 1000) {
                        chatxp -= 1000;
                        chatlevel++;
                    }

                    long voicexp = Main.sqlWorker.getXPVC(m.getGuild().getId(), sender.getUser().getId());

                    int vclevel = 1;

                    while (voicexp > 1000) {
                        voicexp -= 1000;
                        vclevel++;
                    }

                    em.addField("Chat Level", chatlevel + "",true);
                    em.addBlankField(true);
                    em.addField("Voice Level", vclevel + "",true);

                    em.addField("Chat XP", getFormattedXP(Main.sqlWorker.getXP(m.getGuild().getId(), sender.getUser().getId())) + "", true);
                    em.addBlankField(true);
                    em.addField("Voice XP", getFormattedXP(Main.sqlWorker.getXPVC(m.getGuild().getId(), sender.getUser().getId())) + "",true);

                    em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

                    sendMessage(em, m);

                } else {
                    EmbedBuilder em = new EmbedBuilder();

                    em.setThumbnail(messageSelf.getMentionedMembers().get(0).getUser().getAvatarUrl());
                    em.setTitle("Level");

                    long chatxp = Main.sqlWorker.getXP(m.getGuild().getId(), messageSelf.getMentionedMembers().get(0).getUser().getId());

                    int chatlevel = 1;

                    while (chatxp > 1000) {
                        chatxp -= 1000;
                        chatlevel++;
                    }

                    long voicexp = Main.sqlWorker.getXPVC(m.getGuild().getId(), messageSelf.getMentionedMembers().get(0).getUser().getId());

                    int vclevel = 1;

                    while (voicexp > 1000) {
                        voicexp -= 1000;
                        vclevel++;
                    }

                    em.addField("Chat Level", chatlevel + "",true);
                    em.addBlankField(true);
                    em.addField("Voice Level", vclevel + "",true);

                    em.addField("Chat XP", getFormattedXP(Main.sqlWorker.getXP(m.getGuild().getId(), messageSelf.getMentionedMembers().get(0).getUser().getId())) + "", true);
                    em.addBlankField(true);
                    em.addField("Voice XP", getFormattedXP(Main.sqlWorker.getXPVC(m.getGuild().getId(), messageSelf.getMentionedMembers().get(0).getUser().getId())) + "",true);

                    em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

                    sendMessage(em, m);
                }
            } else {
                sendMessage("Not enough Arguments!", m);
                sendMessage("Use ree!level or ree!level @user", m);
            }
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
