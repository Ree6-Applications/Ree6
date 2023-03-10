package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.entities.ScheduledMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.routines.DateValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Schedule a message to a specific time or a repeating time.
 */
@Command(name = "schedule", description = "command.description.schedule", category = Category.COMMUNITY)
public class Schedule implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_SERVER.name()), 5);
            return;
        }

        OptionMapping repeat = commandEvent.getOption("repeat");

        OptionMapping month = commandEvent.getOption("month");
        OptionMapping day = commandEvent.getOption("day");
        OptionMapping hour = commandEvent.getOption("hour");
        OptionMapping minute = commandEvent.getOption("minute");

        long fullTime = 0;
        if (month != null) fullTime += Duration.ofDays(31 * month.getAsLong()).toMillis();
        if (day != null) fullTime += Duration.ofDays(day.getAsLong()).toMillis();
        if (hour != null) fullTime += Duration.ofHours(hour.getAsLong()).toMillis();
        if (minute != null) fullTime += Duration.ofMinutes(minute.getAsLong()).toMillis();

        if (fullTime < Duration.ofMinutes(1).toDays()) {
            commandEvent.reply(commandEvent.getResource("message.default.dateError.notEnough"));
            return;
        }

        // TODO:: check for webhook and use existing one.
        ScheduledMessage scheduledMessage = new ScheduledMessage();
        scheduledMessage.setDelayAmount(fullTime);
        scheduledMessage.setRepeated(repeat.getAsBoolean());
        // TODO:: add success message.
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("schedule", "command.description.schedule")
                                .addOption(OptionType.INTEGER, "month", "The months of the delay.", false)
                                .addOption(OptionType.INTEGER, "day", "The days of the delay.", false)
                                .addOption(OptionType.INTEGER, "hour", "The hours of the delay.", false)
                                .addOption(OptionType.INTEGER, "minute", "The minutes of the delay.", false)
                                .addOption(OptionType.BOOLEAN, "repeat", "If the schedule should be repeated.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
