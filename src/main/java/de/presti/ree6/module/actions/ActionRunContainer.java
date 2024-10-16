package de.presti.ree6.module.actions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A Container class containing the needed Information to run a StreamAction.
 */
@AllArgsConstructor
public class ActionRunContainer {

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
