package de.presti.ree6.actions.streamtools;

import com.github.twitch4j.common.events.TwitchEvent;
import de.presti.ree6.actions.ActionEvent;
import de.presti.ree6.actions.IAction;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to create a Stream Action.
 */
public interface IStreamAction extends IAction {

    /**
     * Run the specific action.
     * @param event The action information.
     *
     * @return True if the action was executed successfully.
     */
    boolean runAction(@NotNull StreamActionEvent event);

    /**
     * Run the specific action.
     * @param event The action information.
     *
     * @return True if the action was executed successfully.
     */
    default boolean runAction(@NotNull ActionEvent event) {
        return runAction((StreamActionEvent) event);
    }

    /**
     * Run the specific action.
     * @param guild The guild.
     * @param twitchEvent The TwitchEvent.
     * @param arguments The arguments.
     *
     * @return True if the action was executed successfully.
     */
    default boolean runAction(@NotNull Guild guild, TwitchEvent twitchEvent, String[] arguments) {
        return runAction(new StreamActionEvent(guild, twitchEvent, arguments));
    }

}
