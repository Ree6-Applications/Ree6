package de.presti.ree6.game.impl.blackjack.util;

import de.presti.ree6.game.impl.blackjack.entities.BlackJackCard;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.ArrayList;

/**
 * Utility class used to provide methods for the game of blackjack.
 */
public class BlackJackCardUtility {

    /**
     * The list of all cards.
     */
    private static final ArrayList<BlackJackCard> allCards = new ArrayList<>();

    /**
     * Method called to add all basic cards to the list.
     */
    public static void loadAllCards() {
        allCards.add(new BlackJackCard(2, Emoji.fromCustom("spade_2", 1025020942130360411L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("spade_3", 1025020944500146267L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("spade_4", 1025020946278518884L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("spade_5", 1025020947700392008L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("spade_6", 1025020949457801308L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("spade_7", 1025020952137977886L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("spade_8", 1025020953681477632L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("spade_9", 1025020955262730300L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_10", 1025020957229854831L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_j", 1025020960547557428L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_q", 1025020964976721950L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("spade_k", 1025020962351104021L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("spade_ace", 1025020959331209217L, false)));

        allCards.add(new BlackJackCard(2, Emoji.fromCustom("heart_2", 1025020942130360411L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("heart_3", 1025020944500146267L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("heart_4", 1025020946278518884L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("heart_5", 1025020947700392008L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("heart_6", 1025020949457801308L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("heart_7", 1025020952137977886L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("heart_8", 1025020953681477632L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("heart_9", 1025020955262730300L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_10", 1025020957229854831L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_j", 1025020960547557428L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_q", 1025020964976721950L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("heart_k", 1025020962351104021L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("heart_ace", 1025020959331209217L, false)));

        allCards.add(new BlackJackCard(2, Emoji.fromCustom("club_2", 1025016863916298280L, false)));
        allCards.add(new BlackJackCard(3, Emoji.fromCustom("club_3", 1025016865199759451L, false)));
        allCards.add(new BlackJackCard(4, Emoji.fromCustom("club_4", 1025016866986524692L, false)));
        allCards.add(new BlackJackCard(5, Emoji.fromCustom("club_5", 1025016868571979876L, false)));
        allCards.add(new BlackJackCard(6, Emoji.fromCustom("club_6", 1025016869767364660L, false)));
        allCards.add(new BlackJackCard(7, Emoji.fromCustom("club_7", 1025016871700926464L, false)));
        allCards.add(new BlackJackCard(8, Emoji.fromCustom("club_8", 1025016873168928768L, false)));
        allCards.add(new BlackJackCard(9, Emoji.fromCustom("club_9", 1025016874464985129L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_10", 1025016875794575371L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_j", 1025016878915145739L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_q", 1025016885928001566L, false)));
        allCards.add(new BlackJackCard(10, Emoji.fromCustom("club_k", 1025016884074139718L, false)));
        allCards.add(new BlackJackCard(11, Emoji.fromCustom("club_ace", 1025016877493268520L, false)));

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

    /**
     * Returns a random card from the default deck.
     * @return A random card from the default deck.
     */
    public static BlackJackCard getRandomCard() {
        if (allCards.isEmpty()) {
            loadAllCards();
        }
        return allCards.get(RandomUtils.secureRandom.nextInt(allCards.size()));
    }

    /**
     * Returns every card of the default deck.
     * @return Every card of the default deck.
     */
    public static ArrayList<BlackJackCard> getAllCards() {
        return allCards;
    }
}
