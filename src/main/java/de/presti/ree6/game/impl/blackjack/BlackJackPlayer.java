package de.presti.ree6.game.impl.blackjack;

import de.presti.ree6.game.core.base.GamePlayer;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer extends GamePlayer {

    private final List<BlackJackCard> inventory = new ArrayList<>();

    public BlackJackPlayer(User relatedUser) {
        super(relatedUser);
    }
    public BlackJackPlayer(long relatedUserId) {
        super(relatedUserId);
    }

    public BlackJackPlayer(GamePlayer gamePlayer) {
        super(gamePlayer.getRelatedUser());
    }

    public List<BlackJackCard> getInventory() {
        return inventory;
    }
}
