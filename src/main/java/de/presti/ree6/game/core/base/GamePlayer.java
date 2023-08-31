package de.presti.ree6.game.core.base;

import de.presti.ree6.bot.BotWorker;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

/**
 * Entity for Games to access Players.
 */
public class GamePlayer {

    /**
     * The ID of the User.
     */
    @Getter
    private final long relatedUserId;

    /**
     * The {@link User} of the Player.
     */
    private User relatedUser;

    /**
     * The InteractionHook of the User.
     */
    @Getter
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
}
