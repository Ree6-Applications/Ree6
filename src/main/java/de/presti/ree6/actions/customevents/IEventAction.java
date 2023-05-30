package de.presti.ree6.actions.customevents;

import de.presti.ree6.actions.ActionEvent;
import de.presti.ree6.actions.IAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to create an Event Action.
 */
public interface IEventAction extends IAction {
    /**
     * Run the specific action.
     * @param event The action information.
     *
     * @return True if the action was executed successfully.
     */
    boolean runAction(@NotNull CustomEventActionEvent event);

    /**
     * Run the specific action.
     * @param event The action information.
     *
     * @return True if the action was executed successfully.
     */
    default boolean runAction(@NotNull ActionEvent event) {
        return runAction((CustomEventActionEvent) event);
    }
}
