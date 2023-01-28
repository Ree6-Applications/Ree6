package de.presti.ree6.streamtools.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to store Information about a StreamAction.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StreamActionInfo {

    /**
     * Get the name of the StreamAction.
     * @return The name of the StreamAction.
     */
    String name();

    /**
     * Get the command of the StreamAction.
     * @return The command of the StreamAction.
     */
    String command();

    /**
     * Get the description of the StreamAction.
     * @return The description of the StreamAction.
     */
    String description();

    /**
     * Get the introduced version of the StreamAction.
     * @return The introduced version of the StreamAction.
     */
    String introduced();
}
