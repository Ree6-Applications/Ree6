package de.presti.ree6.streamtools;

import de.presti.ree6.streamtools.action.IStreamAction;
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
    IStreamAction action;

    /**
     * The Arguments for the Action.
     */
    @Getter(AccessLevel.PUBLIC)
    String[] arguments;
}
