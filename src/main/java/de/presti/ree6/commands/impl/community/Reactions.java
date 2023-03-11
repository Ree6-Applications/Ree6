package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.ReactionRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
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
            OptionMapping action = commandEvent.getOption("action");
            OptionMapping message = commandEvent.getOption("message");
            OptionMapping role = commandEvent.getOption("role");

            switch (action.getAsString()) {
                case "add" -> {
                    if (message == null || role == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    commandEvent.getChannel().retrieveMessageById(message.getAsString()).onErrorMap(x -> {
                       commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return null;
                    }).queue(msg -> {
                        if (msg == null) return;
                        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                        messageCreateBuilder.setContent(commandEvent.getResource("message.reactions.reactionNeeded", role.getAsRole().getAsMention()));
                        msg.reply(messageCreateBuilder.build()).queue();
                    });

                    commandEvent.reply(commandEvent.getResource("message.default.checkBelow"));
                }

                case "remove" -> {
                    if (message == null || role == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    ReactionRole reactionRole = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(),
                            "SELECT * FROM ReactionRole WHERE gid=:gid AND roleId=:roleId AND messageId=:messageId",
                            Map.of("gid", commandEvent.getGuild().getIdLong(), "roleId", role.getAsRole().getIdLong(), "messageId", Long.parseLong(message.getAsString())));

                    if (reactionRole != null) {
                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(reactionRole);

                        commandEvent.reply(commandEvent.getResource("message.reactions.removed", role.getAsRole().getIdLong()), 5);
                    }
                }

                default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("reactions", LanguageService.getDefault("command.description.reactions"))
                .addOption(OptionType.STRING, "action", "The current action that should be performed.", true)
                .addOption(OptionType.STRING, "message", "The ID of the Message.", true)
                .addOption(OptionType.ROLE, "role", "The Role to be given.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
