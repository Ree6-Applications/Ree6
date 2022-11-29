package de.presti.ree6.game.impl.musicquiz.entities;

import de.presti.ree6.game.core.base.GamePlayer;

public class MusicQuizPlayer extends GamePlayer {

    int points = 0;

    public MusicQuizPlayer(long relatedUserId) {
        super(relatedUserId);
    }

    public MusicQuizPlayer(GamePlayer gamePlayer) {
        super(gamePlayer.getRelatedUser());
        setInteractionHook(gamePlayer.getInteractionHook());
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public int getPoints() {
        return points;
    }
}
