package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
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
            Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.perform.slashNotSupported"), commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getArguments().length == 1) {
            if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                Main.getInstance().getSqlConnector().getSqlWorker().removeBirthday(commandEvent.getGuild().getId(), commandEvent.getMember().getId());
                Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.birthday.removed.self"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());

            } else {
                Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.default.usage","birthday add/remove [Birthday(day.month.year)] [@User]"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
        if (commandEvent.getArguments().length == 2) {
            if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                    if (commandEvent.getMessage() != null &&
                            commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.default.noMention.user"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else {
                        Main.getInstance().getSqlConnector().getSqlWorker().removeBirthday(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentions().getMembers().get(0).getId());
                        Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.birthday.removed.other", commandEvent.getMessage().getMentions().getMembers().get(0).getAsMention()), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.birthday.removed.noPerms"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            } else if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                if (GenericValidator.isDate(commandEvent.getArguments()[1], "dd.MM.yyyy", true)) {
                    Main.getInstance().getSqlConnector().getSqlWorker().addBirthday(commandEvent.getGuild().getId(), commandEvent.getChannel().getId(), commandEvent.getMember().getId(), commandEvent.getArguments()[1]);
                    Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.added.self"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.other.dateError"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.default.usage", "birthday add/remove [Birthday(day.month.year)] [@User]"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else if (commandEvent.getArguments().length == 3) {
            if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                if (GenericValidator.isDate(commandEvent.getArguments()[1], "dd.MM.yyyy", true)) {
                    if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        if (commandEvent.getMessage() != null &&
                                commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                            Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.default.noMention.user"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        } else {
                            Main.getInstance().getSqlConnector().getSqlWorker().addBirthday(commandEvent.getGuild().getId(), commandEvent.getChannel().getId(), commandEvent.getMessage().getMentions().getMembers().get(0).getId(), commandEvent.getArguments()[1]);
                            Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.birthday.added.other", commandEvent.getMessage().getMentions().getMembers().get(0).getAsMention()), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        }
                    } else {
                        Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.birthday.added.noPerms"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.other.dateError"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.default.usage","birthday add/remove [Birthday(day.month.year)] [@User]"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.default.usage", "birthday add/remove [Birthday(day.month.year)] [@User]"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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
