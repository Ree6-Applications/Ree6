package de.presti.ree6.module.game.impl.blackjack.entities;

import de.presti.ree6.module.game.core.base.GamePlayer;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Class entity used to represent a player in the game of blackjack.
 */
public class BlackJackPlayer extends GamePlayer {

    /**
     * The cards of the player.
     */
    private final List<BlackJackCard> hand = new ArrayList<>();

    /**
     * @inheritDoc
     */
    public BlackJackPlayer(User relatedUser) {
        super(relatedUser);
    }

    /**
     * @inheritDoc
     */
    public BlackJackPlayer(long relatedUserId) {
        super(relatedUserId);
    }

    /**
     * Constructor.
     * @param gamePlayer The game player.
     */
    public BlackJackPlayer(GamePlayer gamePlayer) {
        super(gamePlayer.getRelatedUser());
        setInteractionHook(gamePlayer.getInteractionHook());
    }

    /**
     * Gets the overall value of the Players hand.
     * @param countHidden Whether to count the value of hidden cards or not.
     * @return The overall value of the Players hand.
     */
    public int getHandValue(boolean countHidden) {
        int value = 0;
        for (BlackJackCard card : hand) {
            if (card.isHidden() && !countHidden) {
                continue;
            }
            value += card.getValue();
        }

        if (value > 21) {
            value = aceCheck(value, countHidden);
        }

        return value;
    }

    /**
     * Gets the hand of the Player as Emojis.
     * @param showHidden Whether to show hidden cards or not.
     * @return The hand of the Player as Emojis.
     */
    public String getHandAsString(boolean showHidden) {
        StringBuilder builder = new StringBuilder();
        for (BlackJackCard card : hand) {
            if (card.isHidden() && !showHidden)
                builder.append("<").append(":").append("card_background").append(":").append("1020328313274781696").append(">").append(" ");
            else
                builder.append(card.getEmoji().getAsMention()).append(" ");
        }
        return builder.toString();
    }

    /**
     * Checks whether the player has an ace and if so, whether the value of the hand is over 21.
     * If so, the value of the ace will be set to 1.
     * @param currentValue The value of the hand.
     * @param checkHidden Whether to check the value of hidden cards or not.
     *
     * @return The value of the hand.
     */
    private int aceCheck(int currentValue, boolean checkHidden) {
        if (currentValue > 21 && hand.stream().anyMatch(blackJackCard -> blackJackCard.getValue() == 11 && (!blackJackCard.isHidden() || checkHidden))) {
            hand.stream().filter(blackJackCard -> blackJackCard.getValue() == 11).findFirst().get().setValue(1);
            currentValue -= 10;
            return aceCheck(currentValue, checkHidden);
        }

        return currentValue;
    }

    /**
     * Gets the hand of the Player.
     * @return The hand of the Player.
     */
    public List<BlackJackCard> getHand() {
        return hand;
    }
}
