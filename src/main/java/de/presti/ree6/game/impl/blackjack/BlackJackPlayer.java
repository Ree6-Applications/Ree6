package de.presti.ree6.game.impl.blackjack;

import de.presti.ree6.game.core.base.GamePlayer;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer extends GamePlayer {

    final List<BlackJackCard> inventory = new ArrayList<>();

    public BlackJackPlayer(User relatedUser) {
        super(relatedUser);
    }

    public BlackJackPlayer(long relatedUserId) {
        super(relatedUserId);
    }
}
