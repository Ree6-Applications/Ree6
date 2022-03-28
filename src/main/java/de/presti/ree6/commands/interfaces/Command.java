package de.presti.ree6.commands.interfaces;

import de.presti.ree6.commands.Category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to store information about the Command easier and access it faster and better.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    /**
     * The Name of the Command.
     *
     * @return command Name.
     */
    String name();

    /**
     * The description of the Command.
     *
     * @return a short and quick notice about the Command.
     */
    String description();

    /**
     * The category of the Command.
     *
     * @return retrieve the category of the Command.
     */
    Category category();
}
