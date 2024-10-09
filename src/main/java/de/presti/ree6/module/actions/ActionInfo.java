package de.presti.ree6.module.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to store Information about Actions.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionInfo {

    /**
     * Get the name of the Action.
     * @return The name of the Action.
     */
    String name();

    /**
     * Get the command of the Action.
     * @return The command of the Action.
     */
    String command();

    /**
     * Get the description of the Action.
     * @return The description of the Action.
     */
    String description();

    /**
     * Get the introduced version of the Action.
     * @return The introduced version of the Action.
     */
    String introduced();
}
