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

        if(args.length != 1) {
            em.setColor(BotUtil.randomEmbedColor());
            em.setTitle("Command Index");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());

            for(Category cat : Category.values()) {
                em.addField("**" + cat.name().toUpperCase().charAt(0) + cat.name().substring(1).toLowerCase() + "**", "ree!help " + cat.name().toLowerCase(), true);
            }

            sendMessage(em, m);
        } else if (args.length == 1) {
            em.setColor(BotUtil.randomEmbedColor());
            em.setTitle("Command Index");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());

            if (isValid(args[0])) {
                
                String end = "";
                
                Category cat = getCategoryFromString(args[0]);
                for(Command cmd : Main.cm.getCommands()) {
                    if(cmd.getCategory() == cat) {
                        end += "``ree!" + cmd.getCmd() + "``\n" + cmd.getDesc() + "\n\n";
                    }
                }

                em.setDescription(end);
            } else {
                for(Category cat : Category.values()) {
                    em.addField("**" + cat.name().toUpperCase().charAt(0) + cat.name().substring(1).toLowerCase() + "**", "ree!help " + cat.name().toLowerCase(), true);
                }
            }

            sendMessage(em, m);

        }
    }

    private boolean isValid(String arg) {
        for(Category cat : Category.values()) {
            if(cat.name().toLowerCase().equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }

    private Category getCategoryFromString(String arg) {
        for(Category cat : Category.values()) {
            if(cat.name().toLowerCase().equalsIgnoreCase(arg)) {
                return cat;
            }
        }

        return null;
    }

}
