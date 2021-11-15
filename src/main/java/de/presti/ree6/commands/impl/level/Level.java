package de.presti.ree6.commands.impl.level;

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

public class Level extends Command {

    public Level() {
        super("level", "Shows the Level of a User!", Category.LEVEL, new String[] {"lvl", "xp", "rank"}, new CommandData("level", "Shows the Level of a User!").addOptions(new OptionData(OptionType.USER, "target", "Show the Level of the User.")));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
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

                    em.setFooter("Requested by " + sender.getUser().getAsTag() + " - " + Data.advertisement, sender.getUser().getAvatarUrl());

                    sendMessage(em, m, hook);

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

                    em.setFooter("Requested by " + sender.getUser().getAsTag() + " - " + Data.advertisement, sender.getUser().getAvatarUrl());

                    sendMessage(em, m, hook);
                }
            } else {
                sendMessage("Not enough Arguments!", m, hook);
                sendMessage("Use " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "level or " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "level @user", m, hook);
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
