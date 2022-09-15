package de.presti.ree6.game.impl.blackjack;

import de.presti.ree6.game.core.GamePlayer;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer extends GamePlayer {

    final InteractionHook interactionHook;
    final List<BlackJackCard> inventory = new ArrayList<>();


    public BlackJackPlayer(long relatedUserId, InteractionHook interactionHook) {
        super(relatedUserId);
        this.interactionHook = interactionHook;
    }
}
