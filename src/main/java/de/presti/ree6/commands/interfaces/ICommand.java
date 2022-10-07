package de.presti.ree6.commands.interfaces;

import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * An Interface class, used to make it easier for the creation of Commands.
 */
public interface ICommand {

    Logger log = LoggerFactory.getLogger(ICommand.class);

    /**
     * Will be fired when the Command is called.
     *
     * @param commandEvent the Event, with every needed data.
     */
    default void onASyncPerform(CommandEvent commandEvent) {
        CompletableFuture.runAsync(() -> onPerform(commandEvent)).exceptionally(throwable -> {
            if (!throwable.getMessage().contains("Unknown Message")) {
                Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.perform.internalError"), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                log.error("An error occurred while executing the command!", throwable);
            }
            return null;
        });
        // Update Stats.
        Main.getInstance().getSqlConnector().getSqlWorker().addStats(commandEvent.getGuild().getId(), this.getClass().getAnnotation(Command.class).name());
    }

    /**
     * Will be fired when the Command is called.
     *
     * @param commandEvent the Event, with every needed data.
     */
    void onPerform(CommandEvent commandEvent);

    /**
     * A CommandData implementation for JDAs SlashCommand Interaction Implementation.
     *
     * @return the created CommandData.
     */
    CommandData getCommandData();

    /**
     * Aliases of the current Command.
     *
     * @return the Aliases.
     */
    String[] getAlias();

}
