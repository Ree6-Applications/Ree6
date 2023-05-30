package de.presti.ree6.actions;

import org.jetbrains.annotations.NotNull;

/**
 * Interface used to create an Action.
 */
public interface IAction {

    /**
     * Run the specific action.
     * @param event The action information.
     *
     * @return True if the action was executed successfully.
     */
    boolean runAction(@NotNull ActionEvent event);
}
