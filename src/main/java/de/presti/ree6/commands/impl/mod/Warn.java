package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

@Command(name = "warn", description = "command.description.warn", category = Category.MOD)
public class Warn implements ICommand {
    /**
     * @param commandEvent the Event, with every needed data.
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MODERATE_MEMBERS.name()), 5);
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MODERATE_MEMBERS.name()), 5);
            return;
        }


    }

    /**
     * @return
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("warn", "command.description.warn")
                .addOption(OptionType.USER, "user", "The User that should be warned!", true)
                .addOption(OptionType.STRING, "reason", "The Reason for the warn!");
    }

    /**
     * @return
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
