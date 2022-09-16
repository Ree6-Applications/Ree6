package de.presti.ree6.game.impl.blackjack.entities;

import de.presti.ree6.game.core.base.GamePlayer;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer extends GamePlayer {

    private final List<BlackJackCard> hand = new ArrayList<>();

    public BlackJackPlayer(User relatedUser) {
        super(relatedUser);
    }

    public BlackJackPlayer(long relatedUserId) {
        super(relatedUserId);
    }

    public BlackJackPlayer(GamePlayer gamePlayer) {
        super(gamePlayer.getRelatedUser());
        setInteractionHook(gamePlayer.getInteractionHook());
    }

    public int getHandValue(boolean showHidden) {
        int value = 0;
        for (BlackJackCard card : hand) {
            if (card.isHidden() && !showHidden) {
                continue;
            }
            value += card.getValue();
        }

        if (value > 21) {
            aceCheck(value, showHidden);
        }

        return value;
    }

    public String getHandAsString(boolean showHidden) {
        StringBuilder builder = new StringBuilder();
        for (BlackJackCard card : hand) {
            if (card.isHidden() && !showHidden)
                builder.append(card.getEmoji().getAsMention()).append(" ");
            else
                builder.append(card.getEmoji().getAsMention()).append(" ");
        }
        return builder.toString();
    }

    private int aceCheck(int currentValue, boolean checkHidden) {
        if (currentValue > 21 && hand.stream().anyMatch(blackJackCard -> blackJackCard.getValue() == 11 && (!blackJackCard.isHidden() || checkHidden))) {
            currentValue -= 10;
            return aceCheck(currentValue, checkHidden);
        }

        return currentValue;
    }

    public List<BlackJackCard> getHand() {
        return hand;
    }
}
