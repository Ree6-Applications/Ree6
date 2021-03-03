package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

public class Help extends Command {

    public Help() {
        super("help", "Shows a list of every Command!", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        EmbedBuilder em = new EmbedBuilder();

        em.setColor(BotUtil.randomEmbedColor());
        em.setTitle("Command Index");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());


        for (Category cat : Category.values()) {
            boolean d = false;
            String end = "";
            for (Command cmds : Main.cm.getCommands()) {
                if (cmds.getCategory() == cat) {
                    end += (d ? "\n" : "") + "ree!" + cmds.getCmd() + " - " + cmds.getDesc();

                    if (!d) {
                        d = true;
                    }
                }
            }
            em.addField("**" + (cat.name().charAt(0) + cat.name().substring(1).toLowerCase()) + "**", end, true);
        }

        m.sendMessage(em.build()).queue();
    }
}
