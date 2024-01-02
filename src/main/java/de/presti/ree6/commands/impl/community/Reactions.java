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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        OptionMapping message = commandEvent.getOption("message");
        OptionMapping role = commandEvent.getOption("role");

        switch (commandEvent.getSubcommand()) {
            case "add" -> {
                if (message == null || role == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                long messageId;

                try {
                    messageId = message.getAsLong();
                } catch (NumberFormatException e) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                commandEvent.getChannel().retrieveMessageById(messageId).onErrorMap(x -> {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return null;
                }).queue(msg -> {
                    if (msg == null) return;
                    MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                    messageCreateBuilder.setContent(LanguageService.getByGuild(commandEvent.getGuild(), "message.reactions.reactionNeeded", role.getAsRole().getAsMention()));
                    msg.reply(messageCreateBuilder.build()).queue();
                });

                commandEvent.reply(commandEvent.getResource("message.default.checkBelow"));
            }

            case "remove" -> {
                if (message == null || role == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                long messageId;

                try {
                    messageId = message.getAsLong();
                } catch (NumberFormatException e) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                ReactionRole reactionRole = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(),
                        "FROM ReactionRole WHERE guildRoleId.guildId=:gid AND guildRoleId.roleId=:roleId AND messageId=:messageId",
                        Map.of("gid", commandEvent.getGuild().getIdLong(), "roleId", role.getAsRole().getIdLong(), "messageId", messageId));

                if (reactionRole != null) {
                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(reactionRole);

                    commandEvent.reply(commandEvent.getResource("message.reactions.removed", role.getAsRole().getIdLong()), 5);
                }
            }

            default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("reactions", LanguageService.getDefault("command.description.reactions"))
                .addSubcommands(new SubcommandData("remove", "Remove a reaction role.")
                                .addOption(OptionType.STRING,"message", "The ID of the Message.", true)
                                .addOption(OptionType.ROLE, "role", "The Role to be given.", true),
                        new SubcommandData("add", "Add a reaction role.")
                                .addOption(OptionType.STRING, "message", "The ID of the Message.", true)
                                .addOption(OptionType.ROLE, "role", "The Role to be given.", true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
