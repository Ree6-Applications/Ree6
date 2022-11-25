package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.ReactionRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Map;

/**
 * A Command to add a Reaction Role.
 */
@Command(name = "reactions", description = "command.description.reactions", category = Category.COMMUNITY)
public class Reactions implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR), 5);
            return;
        }

        if (commandEvent.isSlashCommand()) {
            OptionMapping action = commandEvent.getSlashCommandInteractionEvent().getOption("action");
            OptionMapping message = commandEvent.getSlashCommandInteractionEvent().getOption("messageId");
            OptionMapping role = commandEvent.getSlashCommandInteractionEvent().getOption("roleId");

            switch (action.getAsString()) {
                case "add" -> {
                    if (message == null || role == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    commandEvent.reply(commandEvent.getResource("message.reactions.reactionNeeded", message.getAsString(), role.getAsString()));
                }

                case "remove" -> {
                    if (message == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    ReactionRole reactionRole = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ReactionRole(), "SELECT * FROM ReactionRole WHERE gid=:gid AND roleId=:roleId AND messageId=:messageId", Map.of("gid", commandEvent.getGuild().getIdLong(), "roleId", role.getAsLong(), "messageId", message.getAsLong()));
                    Main.getInstance().getSqlConnector().getSqlWorker().deleteEntity(reactionRole);
                    commandEvent.reply(commandEvent.getResource("message.reactions.removed", message.getAsLong()), 5);
                }

                default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported", commandEvent.getArguments()[0], commandEvent.getArguments()[1]));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("reactions", "command.description.reactions")
                .addOption(OptionType.STRING, "action", "The current action that should be performed.", true)
                .addOption(OptionType.NUMBER, "messageId", "The ID of the Message.", true)
                .addOption(OptionType.ROLE, "roleId", "The Role to be given.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
