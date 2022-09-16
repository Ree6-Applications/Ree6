package de.presti.ree6.game.impl.blackjack.util;

import de.presti.ree6.game.impl.blackjack.entities.BlackJackCard;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.ArrayList;

public class BlackJackCardUtility {

    private static final ArrayList<BlackJackCard> allCards = new ArrayList<>();

    public static void loadAllCards() {
        allCards.add(new BlackJackCard(2, Emoji.fromCustom("spade_2", 0L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("spade_3", 0L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("spade_4", 0L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("spade_5", 0L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("spade_6", 0L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("spade_7", 0L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("spade_8", 0L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("spade_9", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_10", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_j", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_q", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_k", 0L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("spade_ace", 0L, false)));

        allCards.add(new BlackJackCard(2, Emoji.fromCustom("heart_2", 0L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("heart_3", 0L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("heart_4", 0L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("heart_5", 0L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("heart_6", 0L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("heart_7", 0L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("heart_8", 0L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("heart_9", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_10", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_j", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_q", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_k", 0L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("heart_ace", 0L, false)));

        allCards.add(new BlackJackCard(2, Emoji.fromCustom("club_2", 0L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("club_3", 0L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("club_4", 0L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("club_5", 0L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("club_6", 0L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("club_7", 0L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("club_8", 0L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("club_9", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_10", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_j", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_q", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_k", 0L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("club_ace", 0L, false)));

        allCards.add(new BlackJackCard(2, Emoji.fromCustom("diamond_2", 0L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("diamond_3", 0L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("diamond_4", 0L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("diamond_5", 0L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("diamond_6", 0L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("diamond_7", 0L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("diamond_8", 0L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("diamond_9", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("diamond_10", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("diamond_j", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("diamond_q", 0L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("diamond_k", 0L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("diamond_ace", 0L, false)));
    }

    public static BlackJackCard getRandomCard() {
        if (allCards.isEmpty()) {
            loadAllCards();
        }
        return allCards.get(RandomUtils.secureRandom.nextInt(allCards.size()));
    }

    public static ArrayList<BlackJackCard> getAllCards() {
        return allCards;
    }
}
