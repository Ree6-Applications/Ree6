package de.presti.ree6.game.core;

public class GamePlayer {

    private final long relatedUserId;

    public GamePlayer(long relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public long getRelatedUserId() {
        return relatedUserId;
    }
}
