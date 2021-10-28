package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Help extends Command {

    public Help() {
        super("help", "Shows a list of every Command!", Category.INFO, new CommandData("help", "Shows a list of every Command!").addOptions(new OptionData(OptionType.STRING, "name", "Show every Command of a Category.")));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {

        EmbedBuilder em = new EmbedBuilder();

        if (args.length != 1) {
            em.setColor(BotUtil.randomEmbedColor());
            em.setTitle("Command Index");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setFooter(m.getGuild().getName() + " - " + Data.advertisement, m.getGuild().getIconUrl());

            for (Category cat : Category.values()) {
                if (cat != Category.HIDDEN) {
                    em.addField("**" + cat.name().toUpperCase().charAt(0) + cat.name().substring(1).toLowerCase() + "**",  Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "help " + cat.name().toLowerCase(), true);
                }
            }

            sendMessage(em, m, hook);
        } else if (args.length == 1) {
            em.setColor(BotUtil.randomEmbedColor());
            em.setTitle("Command Index");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setFooter(m.getGuild().getName() + " - " + Data.advertisement, m.getGuild().getIconUrl());

            if (isValid(args[0])) {

                StringBuilder end = new StringBuilder();

                Category cat = getCategoryFromString(args[0]);
                for (Command cmd : Main.commandManager.getCommands()) {
                    if (cmd.getCategory() == cat) {
                        end.append("``"  + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue()).append(cmd.getCmd()).append("``\n").append(cmd.getDesc()).append("\n\n");
                    }
                }

                em.setDescription(end.toString());
            } else {
                for (Category cat : Category.values()) {
                    if (cat != Category.HIDDEN) {
                        em.addField("**" + cat.name().toUpperCase().charAt(0) + cat.name().substring(1).toLowerCase() + "**",  Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "help " + cat.name().toLowerCase(), true);
                    }
                }
            }

            sendMessage(em, m, hook);

        }
    }

    private boolean isValid(String arg) {
        for (Category cat : Category.values()) {
            if (cat.name().toLowerCase().equalsIgnoreCase(arg) && cat != Category.HIDDEN) {
                return true;
            }
        }

        return false;
    }

    private Category getCategoryFromString(String arg) {
        for (Category cat : Category.values()) {
            if (cat.name().toLowerCase().equalsIgnoreCase(arg)) {
                return cat;
            }
        }

        return null;
    }

}
