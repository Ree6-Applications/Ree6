package de.presti.ree6.game.impl.blackjack.entities;

import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

public class BlackJackCard {

    private final int value;
    private final CustomEmoji emoji;
    private boolean isHidden = true;

    public BlackJackCard(int value, CustomEmoji emoji) {
        this.value = value;
        this.emoji = emoji;
    }

    public int getValue() {
        return value;
    }

    public CustomEmoji getEmoji() {
        return emoji;
    }
    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
}
