package de.presti.ree6.game.core.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for internal Games to access Information.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GameInfo {

    /**
     * The Name of the Game.
     * @return The Name of the Game.
     */
    String name();

    /**
     * The Description of the Game.
     * @return The Description of the Game.
     */
    String description();

    /**
     * The minimal Amount of Players.
     * @return The minimal Amount of Players.
     */
    int minPlayers();

    /**
     * The maximal Amount of Players.
     * @return The maximal Amount of Players.
     */
    int maxPlayers();
}
