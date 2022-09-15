package de.presti.ree6.game.core;

import de.presti.ree6.bot.BotWorker;
import net.dv8tion.jda.api.entities.User;

public class GamePlayer {

    private final long relatedUserId;
    private User relatedUser;

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
}
