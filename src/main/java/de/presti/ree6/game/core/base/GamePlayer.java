package de.presti.ree6.game.core.base;

import de.presti.ree6.bot.BotWorker;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

/**
 * Entity for Games to access Players.
 */
public class GamePlayer {

    /**
     * The ID of the User.
     */
    private final long relatedUserId;

    /**
     * The {@link User} of the Player.
     */
    private User relatedUser;

    /**
     * The InteractionHook of the User.
     */
    private InteractionHook interactionHook;

    /**
     * Constructor for the GamePlayer.
     * @param relatedUser The {@link User} of the Player.
     */
    public GamePlayer(User relatedUser) {
        this.relatedUserId = relatedUser.getIdLong();
        this.relatedUser = relatedUser;
    }

    /**
     * Constructor for the GamePlayer.
     * @param relatedUserId The ID of the User.
     */
    public GamePlayer(long relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    /**
     * Get the ID of the User.
     * @return The ID of the User.
     */
    public long getRelatedUserId() {
        return relatedUserId;
    }

    /**
     * Get the {@link User} of the Player.
     * @return The {@link User} of the Player.
     */
    public User getRelatedUser() {
        if (relatedUser == null)
            return relatedUser = BotWorker.getShardManager().getUserById(relatedUserId);

        return relatedUser;
    }

    /**
     * Set the {@link InteractionHook} of the User.
     * @param interactionHook The {@link InteractionHook} of the User.
     */
    public void setInteractionHook(InteractionHook interactionHook) {
        this.interactionHook = interactionHook;
    }

    /**
     * Get the InteractionHook of the User.
     * @return The InteractionHook of the User.
     */
    public InteractionHook getInteractionHook() {
        return interactionHook;
    }
}
