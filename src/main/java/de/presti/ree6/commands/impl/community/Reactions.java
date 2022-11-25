package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

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
            OptionMapping message = commandEvent.getSlashCommandInteractionEvent().getOption("messageId");
            OptionMapping role = commandEvent.getSlashCommandInteractionEvent().getOption("roleId");

            commandEvent.reply(commandEvent.getResource("message.reactions.reactionNeeded", message.getAsString(), role.getAsString()));
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
