package de.presti.ree6.module.actions.customevents;

import de.presti.ree6.module.actions.ActionEvent;
import de.presti.ree6.module.actions.IAction;
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
