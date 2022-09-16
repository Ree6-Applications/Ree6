package de.presti.ree6.game.core.base;

import de.presti.ree6.bot.BotWorker;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class GamePlayer {

    private final long relatedUserId;
    private User relatedUser;

    private InteractionHook interactionHook;

    public GamePlayer(User relatedUser) {
        this.relatedUserId = relatedUser.getIdLong();
        this.relatedUser = relatedUser;
    }

    public GamePlayer(long relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public long getRelatedUserId() {
        return relatedUserId;
    }

    public User getRelatedUser() {
        if (relatedUser == null)
            return relatedUser = BotWorker.getShardManager().getUserById(relatedUserId);

        return relatedUser;
    }

    public void setInteractionHook(InteractionHook interactionHook) {
        this.interactionHook = interactionHook;
    }

    public InteractionHook getInteractionHook() {
        return interactionHook;
    }
}
