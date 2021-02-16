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

                    int xp = Main.sqlWorker.getXP(m.getGuild().getId(), sender.getUser().getId());

                    int level = 1;

                    while (xp > 1000) {
                        xp -= 1000;
                        level++;
                    }

                    em.addField("Level", level + "",true);
                    em.addField("XP", Main.sqlWorker.getXP(m.getGuild().getId(), sender.getUser().getId()) + "", true);
                    em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

                    sendMessage(em, m);

                } else {
                    EmbedBuilder em = new EmbedBuilder();

                    em.setThumbnail(messageSelf.getMentionedMembers().get(0).getUser().getAvatarUrl());
                    em.setTitle("Level");

                    int xp = Main.sqlWorker.getXP(m.getGuild().getId(), messageSelf.getMentionedMembers().get(0).getUser().getId());

                    int level = 1;

                    while (xp > 1000) {
                        xp -= 1000;
                        level++;
                    }

                    em.addField("Level", level + "",true);
                    em.addField("XP", Main.sqlWorker.getXP(m.getGuild().getId(), messageSelf.getMentionedMembers().get(0).getUser().getId()) + "", true);
                    em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

                    sendMessage(em, m);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!level or ree!level @user", 5, m);
            }
    }
}
