package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to opt-out of data collection.
 */
@Command(name = "optout", description = "command.description.optout", category = Category.INFO)
public class Optout implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (SQLSession.getSqlConnector().getSqlWorker().isOptOut(commandEvent.getGuild().getIdLong(), commandEvent.getMember().getIdLong())) {
            SQLSession.getSqlConnector().getSqlWorker().optIn(commandEvent.getGuild().getIdLong(), commandEvent.getMember().getIdLong());
            commandEvent.reply(commandEvent.getResource("message.optout.optedIn"));
        } else {
            SQLSession.getSqlConnector().getSqlWorker().optOut(commandEvent.getGuild().getIdLong(), commandEvent.getMember().getIdLong());
            commandEvent.reply(commandEvent.getResource("message.optout.optedOut"));
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
        return new String[] { "opt-out", "out", "opt", "privacy" };
    }
}
