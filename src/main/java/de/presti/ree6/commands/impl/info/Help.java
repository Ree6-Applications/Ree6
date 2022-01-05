package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.stream.Collectors;

public class Help extends Command {

    public Help() {
        super("help", "Shows a list of every Command!", Category.INFO, new CommandData("help", "Shows a list of every Command!")
                .addOptions(new OptionData(OptionType.STRING, "category", "Which Category you want to check out.")));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping categoryOption = commandEvent.getSlashCommandEvent().getOption("category");

            if (categoryOption != null) {
                sendHelpInformation(categoryOption.getAsString(), commandEvent);
            } else {
                sendHelpInformation(null, commandEvent);
            }
        } else {

            if (commandEvent.getArguments().length != 1) {
                sendHelpInformation(null, commandEvent);
            } else if (commandEvent.getArguments().length == 1) {
                sendHelpInformation(commandEvent.getArguments()[0], commandEvent);
            }
        }
    }

    public void sendHelpInformation(String categoryString, CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        em.setColor(BotUtil.randomEmbedColor());
        em.setTitle("Command Index");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        if (categoryString == null) {
            for (Category cat : Category.values()) {
                if (cat != Category.HIDDEN) {
                    em.addField("**" + cat.name().toUpperCase().charAt(0) + cat.name().substring(1).toLowerCase() + "**",  Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "help " + cat.name().toLowerCase(), true);
                }
            }
        } else {
            if (isValid(categoryString)) {
                StringBuilder end = new StringBuilder();

                Category category = getCategoryFromString(commandEvent.getArguments()[0]);
                for (Command cmd : Main.getInstance().getCommandManager().getCommands().stream().filter(command -> command.getCategory() == category).collect(Collectors.toList())) {
                        end.append("``")
                                .append(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue())
                                .append(cmd.getCmd())
                                .append("``\n")
                                .append(cmd.getDesc())
                                .append("\n\n");
                }

                em.setDescription(end.toString());
            } else {
                sendHelpInformation(null, commandEvent);
            }
        }

        sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
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
