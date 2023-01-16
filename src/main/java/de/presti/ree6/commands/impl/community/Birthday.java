package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.commons.validator.GenericValidator;

/**
 * This command is used to let the bot remember your Birthday.
 */
@Command(name = "birthday", description = "command.description.birthday", category = Category.COMMUNITY)
public class Birthday implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.slashNotSupported"));
            return;
        }

        if (commandEvent.getArguments().length == 1) {
            if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                SQLSession.getSqlConnector().getSqlWorker().removeBirthday(commandEvent.getGuild().getId(), commandEvent.getMember().getId());
                commandEvent.reply(commandEvent.getResource("message.birthday.removed.self"), 5);

            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","birthday add/remove [Birthday(day.month.year)] [@User]"), 5);
            }
        }
        if (commandEvent.getArguments().length == 2) {
            if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                    if (commandEvent.getMessage() != null &&
                            commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                    } else {
                        SQLSession.getSqlConnector().getSqlWorker().removeBirthday(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentions().getMembers().get(0).getId());
                        commandEvent.reply(commandEvent.getResource("message.birthday.removed.other", commandEvent.getMessage().getMentions().getMembers().get(0).getAsMention()), 5);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.birthday.removed.noPerms"), 5);
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                if (GenericValidator.isDate(commandEvent.getArguments()[1], "dd.MM.yyyy", true)) {
                    SQLSession.getSqlConnector().getSqlWorker().addBirthday(commandEvent.getGuild().getId(), commandEvent.getChannel().getId(), commandEvent.getMember().getId(), commandEvent.getArguments()[1]);
                    commandEvent.reply(commandEvent.getResource("message.birthday.added.self"), 5);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.birthday.other.dateError"), 5);
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage", "birthday add/remove [Birthday(day.month.year)] [@User]"), 5);
            }
        } else if (commandEvent.getArguments().length == 3) {
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                if (GenericValidator.isDate(commandEvent.getArguments()[1], "dd.MM.yyyy", true)) {
                    if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        if (commandEvent.getMessage() != null &&
                                commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                            commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                        } else {
                            SQLSession.getSqlConnector().getSqlWorker().addBirthday(commandEvent.getGuild().getId(), commandEvent.getChannel().getId(), commandEvent.getMessage().getMentions().getMembers().get(0).getId(), commandEvent.getArguments()[1]);
                            commandEvent.reply(commandEvent.getResource("message.birthday.added.other", commandEvent.getMessage().getMentions().getMembers().get(0).getAsMention()), 5);
                        }
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.birthday.added.noPerms"), 5);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.birthday.other.dateError"), 5);
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","birthday add/remove [Birthday(day.month.year)] [@User]"), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.usage", "birthday add/remove [Birthday(day.month.year)] [@User]"), 5);
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"bday"};
    }
}
