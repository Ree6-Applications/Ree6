package de.presti.ree6.game.core.base;

public @interface GameInfo {

    String name();

    String description();

    int minPlayers();

    int maxPlayers();
}
