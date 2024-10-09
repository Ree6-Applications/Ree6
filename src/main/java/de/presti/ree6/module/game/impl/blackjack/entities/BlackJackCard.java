package de.presti.ree6.module.game.impl.blackjack.entities;

import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

/**
 * Class entity used to represent a card in the game of blackjack.
 */
public class BlackJackCard {

    /**
     * The value of the card.
     */
    private int value;

    /**
     * The emoji of the card.
     */
    private final CustomEmoji emoji;

    /**
     * Whether the card is hidden or not.
     */
    private boolean isHidden = true;

    /**
     * Creates a new card.
     * @param value The value of the card.
     * @param emoji The emoji of the card.
     */
    public BlackJackCard(int value, CustomEmoji emoji) {
        this.value = value;
        this.emoji = emoji;
    }

    /**
     * Gets the value of the card.
     * @return The value of the card.
     */
    public int getValue() {
        return value;
    }

    /**
     * Set the value of the card.
     * @param newValue the new value.
     */
    public void setValue(int newValue) {
        value = newValue;
    }

    /**
     * Gets the emoji of the card.
     * @return The emoji of the card.
     */
    public CustomEmoji getEmoji() {
        return emoji;
    }

    /**
     * Gets whether the card is hidden or not.
     * @return Whether the card is hidden or not.
     */
    public boolean isHidden() {
        return isHidden;
    }

    /**
     * Sets whether the card is hidden or not.
     * @param hidden Whether the card is hidden or not.
     */
    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
}
