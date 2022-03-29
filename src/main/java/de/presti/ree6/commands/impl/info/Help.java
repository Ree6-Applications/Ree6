package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

@Command(name = "help", description = "Shows a list of every Command!", category = Category.INFO)
public class Help implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping categoryOption = commandEvent.getSlashCommandInteractionEvent().getOption("category");

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
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
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

                Category category = getCategoryFromString(categoryString);
                for (ICommand cmd : Main.getInstance().getCommandManager().getCommands().stream().filter(command -> command.getClass().getAnnotation(Command.class).category() == category).toList()) {
                        end.append("``")
                                .append(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue())
                                .append(cmd.getClass().getAnnotation(Command.class).name())
                                .append("``\n")
                                .append(cmd.getClass().getAnnotation(Command.class).description())
                                .append("\n\n");
                }

                em.setDescription(end.toString());
            } else {
                sendHelpInformation(null, commandEvent);
                return;
            }
        }

        Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("help", "Shows a list of every Command!")
                .addOptions(new OptionData(OptionType.STRING, "category", "Which Category you want to check out."));
    }

    private boolean isValid(String arg) {
        if (arg == null) return false;

        for (Category cat : Category.values()) {
            if (cat.name().equalsIgnoreCase(arg) && cat != Category.HIDDEN) {
                return true;
            }
        }

        return false;
    }

    private Category getCategoryFromString(String arg) {
        if (arg == null) return null;
        for (Category cat : Category.values()) {
            if (cat.name().equalsIgnoreCase(arg)) {
                return cat;
            }
        }

        return null;
    }

}
