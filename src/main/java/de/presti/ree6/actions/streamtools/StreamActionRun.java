package de.presti.ree6.actions.streamtools;

import de.presti.ree6.actions.IAction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A Container class containing the needed Information to run a StreamAction.
 */
@AllArgsConstructor
public class StreamActionRun {

    /**
     * The Action to run.
     */
    @Getter(AccessLevel.PUBLIC)
    IAction action;

    /**
     * The Arguments for the Action.
     */
    @Getter(AccessLevel.PUBLIC)
    String[] arguments;
}
