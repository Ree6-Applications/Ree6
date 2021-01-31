package de.presti.ree6.commands.impl;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

public class Help extends Command {

    public Help() {
        super("help", "Shows a list of every Command!");
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        EmbedBuilder em = new EmbedBuilder();

        em.setColor(BotUtil.randomEmbedColor());
        em.setTitle("Command Index", BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());

        String end = "";
        boolean d = false;

        for(Command cmds : Main.cm.getCommands()) {

            end += (d ? "\n" : "") + "ree!" + cmds.getCmd() + " - " + cmds.getDesc();

            if(!d) {
                d = true;
            }
        }

        em.addField("**Commands**",  end, true);
        m.sendMessage(em.build()).queue();
        messageSelf.delete().queue();
    }
}
