package de.presti.ree6.module.game.impl.musicquiz.entities;

import de.presti.ree6.module.game.core.base.GamePlayer;
import lombok.Getter;

/**
 * Class used to store information about a player in the music quiz game.
 */
@Getter
public class MusicQuizPlayer extends GamePlayer {

    /**
     * The amount of points the player has.
     */
    int points = 0;

    /**
     * @see GamePlayer#GamePlayer(long)
     */
    public MusicQuizPlayer(long relatedUserId) {
        super(relatedUserId);
    }

    /**
     * Constructor to pass a GamePlayer to a MusicQuizPlayer
     */
    public MusicQuizPlayer(GamePlayer gamePlayer) {
        super(gamePlayer.getRelatedUser());
        setInteractionHook(gamePlayer.getInteractionHook());
    }

    /**
     * Method used to add points to the player.
     * @param points The number of points that should be added.
     */
    public void addPoints(int points) {
        this.points += points;
    }
}
