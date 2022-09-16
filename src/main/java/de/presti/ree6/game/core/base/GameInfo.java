package de.presti.ree6.game.core.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GameInfo {

    String name();

    String description();

    int minPlayers();

    int maxPlayers();
}
