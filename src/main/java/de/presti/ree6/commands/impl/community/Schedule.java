package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

@Command(name = "schedule", description = "command.description.schedule", category = Category.COMMUNITY)
public class Schedule implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {

    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("schedule", "command.description.schedule")
                .addSubcommands(new SubcommandData("in", "Send a scheduled message in x time.")
                                .addOption(OptionType.INTEGER, "hour", "The hours of the delay.")
                                .addOption(OptionType.INTEGER, "minute", "The minutes of the delay.")
                                .addOption(OptionType.BOOLEAN, "repeat", "If the schedule should be repeated."),
                        new SubcommandData("at", "Send a scheduled message at x.")
                                .addOption(OptionType.STRING, "date", "The date in dd.MM.yyyy format.")
                                .addOption(OptionType.STRING, "time", "The time in the hh/mm 24(hour)-format.")
                                .addOption(OptionType.INTEGER, "repeat", "If the schedule should be repeated."));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
