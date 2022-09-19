package de.presti.ree6.game.impl.blackjack;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.core.GameSession;
import de.presti.ree6.game.core.base.GameInfo;
import de.presti.ree6.game.core.base.GamePlayer;
import de.presti.ree6.game.core.base.IGame;
import de.presti.ree6.game.impl.blackjack.entities.BlackJackCard;
import de.presti.ree6.game.impl.blackjack.entities.BlackJackPlayer;
import de.presti.ree6.game.impl.blackjack.util.BlackJackCardUtility;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.ArrayList;

/**
 * Class used to represent the game of blackjack.
 */
@GameInfo(name = "Blackjack", description = "Play Blackjack with your friends!", minPlayers = 2, maxPlayers = 2)
public class Blackjack implements IGame {

    /**
     * The game session.
     */
    private final GameSession session;

    /**
     * The two Blackjack players.
     */
    BlackJackPlayer player, playerTwo;

    /**
     * The cards of the dealer.
     */
    ArrayList<String> usedCards = new ArrayList<>();

    /**
     * Value to remember if the last player performed the action of "stand".
     */
    boolean standUsed;

    /**
     * The Player which has the turn.
     */
    BlackJackPlayer currentPlayer;

    /**
     * Constructor.
     * @param gameSession The game session.
     */
    public Blackjack(GameSession gameSession) {
        session = gameSession;
        createGame();
    }

    /**
     * The message of the game.
     */
    Message menuMessage;

    /**
     * @inheritDoc
     */
    @Override
    public void createGame() {
        if (session.getParticipants().isEmpty() || session.getParticipants().size() > 2) {
            Main.getInstance().getCommandManager().sendMessage("You need to have 2 participants to play this game!", 5, session.getChannel());
            stopGame();
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Blackjack");
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setDescription("Welcome to Blackjack! You can start the game by clicking the button below!" +
                "\nBefore you can start it thou, you will need someone else to play with you!" +
                "\nThey will need to use /game join " + session.getGameIdentifier() + " to join the game!");

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("game_start:" + session.getGameIdentifier(), "Start Game").asDisabled());
        //messageCreateBuilder.setActionRow(Button.secondary("game_join:" + session.getGameIdentifier(), "Join Game").asEnabled());
        session.getChannel().sendMessage(messageCreateBuilder.build()).queue(message -> menuMessage = message);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void startGame() {
        BlackJackCard card = getRandomCard();

        card.setHidden(false);
        player.getHand().add(card);
        player.getHand().add(getRandomCard());

        card = getRandomCard();
        card.setHidden(false);
        playerTwo.getHand().add(card);
        playerTwo.getHand().add(getRandomCard());

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Blackjack");
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setAuthor(player.getRelatedUser().getAsTag(), null, player.getRelatedUser().getAvatarUrl());
        embedBuilder.addField("**Your Cards**", player.getHandAsString(true) + "\n\nYour Value: " + player.getHandValue(true), true);
        embedBuilder.addField(playerTwo.getRelatedUser().getAsTag() + "s **Cards**", playerTwo.getHandAsString(false) + "\n\nTheir Value: " + playerTwo.getHandValue(false), true);

        embedBuilder.setFooter("It's your turn!");

        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.primary("game_blackjack_hit", "Hit"), Button.success("game_blackjack_stand", "Stand"), Button.secondary("game_blackjack_doubledown", "Double Down"));

        player.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

        embedBuilder.setAuthor(playerTwo.getRelatedUser().getAsTag(), null, playerTwo.getRelatedUser().getAvatarUrl());
        embedBuilder.clearFields();
        embedBuilder.addField("**Your Cards**", playerTwo.getHandAsString(true) + "\n\nYour Value: " + playerTwo.getHandValue(true), true);
        embedBuilder.addField(player.getRelatedUser().getAsTag() + "s **Cards**", player.getHandAsString(false) + "\n\nTheir Value: " + player.getHandValue(false), true);

        embedBuilder.setFooter("Wait for your turn!");

        messageEditBuilder.setEmbeds(embedBuilder.build());
        playerTwo.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
        Main.getInstance().getCommandManager().deleteMessage(menuMessage, null);
        currentPlayer = player;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void joinGame(GamePlayer user) {
        if (player != null && playerTwo != null) {
            player.getInteractionHook().editOriginal("The game is full!").queue();
            return;
        }

        if ((player != null && user.getRelatedUserId() == player.getRelatedUserId()) || (playerTwo != null && user.getRelatedUserId() == playerTwo.getRelatedUserId())) {
            user.getInteractionHook().editOriginal("You are already in this game!").queue();
            return;
        }

        if (player == null) {
            player = new BlackJackPlayer(user);
        } else {
            playerTwo = new BlackJackPlayer(user);
        }

        if (player != null && playerTwo != null) {
            MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
            messageEditBuilder.applyMessage(menuMessage);
            EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));
            embedBuilder.setDescription("The minimal amount of Players have been reached! You can start the game by clicking the button below!");
            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setActionRow(Button.success("game_start:" + session.getGameIdentifier(), "Start Game").asEnabled());
            menuMessage.editMessage(messageEditBuilder.build()).queue();

            messageEditBuilder.clear();
            embedBuilder.setDescription("You have joined the Game!\nPlease wait for the other player to start the game!");
            messageEditBuilder.setEmbeds(embedBuilder.build());
            playerTwo.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

            messageEditBuilder.clear();
            embedBuilder.setDescription("The minimal amount of Players have been reached! You can start the game by clicking the button on the Game Message!");
            messageEditBuilder.setEmbeds(embedBuilder.build());
            player.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void leaveGame(GamePlayer user) {
        if (player != null && player.getRelatedUserId() == user.getRelatedUserId()) {
            player = null;
        } else if (playerTwo != null && playerTwo.getRelatedUserId() == user.getRelatedUserId()) {
            playerTwo = null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onReactionReceive(GenericMessageReactionEvent messageReactionEvent) {

    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReceive(MessageReceivedEvent messageReceivedEvent) {

    }

    /**
     * @inheritDoc
     */
    @Override
    public void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent) {
        if (currentPlayer == null) {
            return;
        }

        if (currentPlayer.getRelatedUserId() != buttonInteractionEvent.getUser().getIdLong()) {
            buttonInteractionEvent.deferEdit().queue();
            return;
        }

        switch (buttonInteractionEvent.getComponentId()) {
            case "game_blackjack_hit" -> {
                if (player.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    hit(player, playerTwo);
                } else if (playerTwo.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    hit(playerTwo, player);
                }
                buttonInteractionEvent.deferEdit().queue();
            }
            case "game_blackjack_stand" -> {
                if (player.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    stand(player, playerTwo);
                } else if (playerTwo.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    stand(playerTwo, player);
                }
                buttonInteractionEvent.deferEdit().queue();
            }
            case "game_blackjack_doubledown" -> {
                if (player.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    doubleDown(player, playerTwo);
                } else if (playerTwo.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    doubleDown(playerTwo, player);
                }
                buttonInteractionEvent.deferEdit().queue();
            }
        }
    }

    /**
     * Get a random card from the basic Deck, but check if the card is already a players hand.
     * @return The random card.
     */
    public BlackJackCard getRandomCard() {
        BlackJackCard card = BlackJackCardUtility.getRandomCard();

        if (usedCards.size() == BlackJackCardUtility.getAllCards().size()) {
            return null;
        }

        if (usedCards.contains(card.getEmoji().getName())) {
            return getRandomCard();
        } else {
            usedCards.add(card.getEmoji().getName());
            return card;
        }
    }

    /**
     * Hit the player and give him a new card.
     * @param player The player who hit.
     * @param playerTwo The other player.
     */
    public void hit(BlackJackPlayer player, BlackJackPlayer playerTwo) {
        standUsed = false;

        BlackJackCard card = getRandomCard();

        if (card == null) {
            stopGame();
            return;
        }

        player.getHand().add(card);

        if (player.getHandValue(true) > 21) {
            stopGame(player, playerTwo);
        } else {
            MessageEditBuilder messageEditBuilder = new MessageEditBuilder();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Blackjack");
            embedBuilder.setColor(BotWorker.randomEmbedColor());
            embedBuilder.setAuthor(player.getRelatedUser().getAsTag(), null, player.getRelatedUser().getAvatarUrl());
            embedBuilder.addField("**Your Cards**", player.getHandAsString(true) + "\n\nYour Value: " + player.getHandValue(true), true);
            embedBuilder.addField(playerTwo.getRelatedUser().getAsTag() + "s **Cards**", playerTwo.getHandAsString(false) + "\n\nTheir Value: " + playerTwo.getHandValue(false), true);

            embedBuilder.setFooter("Wait for your turn!");

            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setActionRow(Button.primary("game_blackjack_hit", "Hit"), Button.success("game_blackjack_stand", "Stand"), Button.secondary("game_blackjack_doubledown", "Double Down"));

            player.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

            embedBuilder.setAuthor(playerTwo.getRelatedUser().getAsTag(), null, playerTwo.getRelatedUser().getAvatarUrl());
            embedBuilder.clearFields();
            embedBuilder.addField("**Your Cards**", playerTwo.getHandAsString(true) + "\n\nYour Value: " + playerTwo.getHandValue(true), true);
            embedBuilder.addField(player.getRelatedUser().getAsTag() + "s **Cards**", player.getHandAsString(false) + "\n\nTheir Value: " + player.getHandValue(false), true);
            embedBuilder.setFooter("It's your turn!");
            messageEditBuilder.setEmbeds(embedBuilder.build());
            playerTwo.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
            currentPlayer = playerTwo;
        }
    }

    /**
     * Stand the player and let the other player play.
     * @param player The player who stand.
     * @param playerTwo The other player.
     */
    public void stand(BlackJackPlayer player, BlackJackPlayer playerTwo) {
        if (standUsed) {
            stopGame(player, playerTwo);
        } else {
            standUsed = true;
        }

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Blackjack");
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setAuthor(player.getRelatedUser().getAsTag(), null, player.getRelatedUser().getAvatarUrl());
        embedBuilder.addField("**Your Cards**", player.getHandAsString(true) + "\n\nYour Value: " + player.getHandValue(true), true);
        embedBuilder.addField(playerTwo.getRelatedUser().getAsTag() + "s **Cards**", playerTwo.getHandAsString(false) + "\n\nTheir Value: " + playerTwo.getHandValue(false), true);

        embedBuilder.setFooter("Wait for your turn!");

        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.primary("game_blackjack_hit", "Hit"), Button.success("game_blackjack_stand", "Stand"), Button.secondary("game_blackjack_doubledown", "Double Down"));

        player.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

        embedBuilder.setAuthor(playerTwo.getRelatedUser().getAsTag(), null, playerTwo.getRelatedUser().getAvatarUrl());
        embedBuilder.clearFields();
        embedBuilder.addField("**Your Cards**", playerTwo.getHandAsString(true) + "\n\nYour Value: " + playerTwo.getHandValue(true), true);
        embedBuilder.addField(player.getRelatedUser().getAsTag() + "s **Cards**", player.getHandAsString(false) + "\n\nTheir Value: " + player.getHandValue(false), true);
        embedBuilder.setFooter("It's your turn!");
        messageEditBuilder.setEmbeds(embedBuilder.build());
        playerTwo.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
        currentPlayer = playerTwo;
    }

    /**
     * Double down the player and give him a new card.
     * @param player The player who double down.
     * @param playerTwo The other player.
     */
    public void doubleDown(BlackJackPlayer player, BlackJackPlayer playerTwo) {
        currentPlayer = playerTwo;
    }

    /**
     * Stop the game and check who won.
     * @param player The player who won.
     * @param playerTwo The other player.
     */
    public void stopGame(BlackJackPlayer player, BlackJackPlayer playerTwo) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Blackjack");
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        BlackJackPlayer winner = findWinner();

        embedBuilder.setDescription("The Game has ended!\nThe Result is: " + (winner == null ? "No one because its a Draw" : winner.getRelatedUser().getAsTag() + " has won!"));

        embedBuilder.addField(player.getRelatedUser().getAsTag()+ "s **Cards**", player.getHandAsString(true) + "\n\nValue: " + player.getHandValue(true), true);
        embedBuilder.addField(playerTwo.getRelatedUser().getAsTag() + "s **Cards**", playerTwo.getHandAsString(true) + "\n\nValue: " + playerTwo.getHandValue(true), true);

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setComponents(new ArrayList<>());
        player.getInteractionHook().editOriginalComponents(new ArrayList<>()).queue();
        playerTwo.getInteractionHook().editOriginalComponents(new ArrayList<>()).queue();

        Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), session.getChannel());
        stopGame();
    }

    /**
     * Determine the winner of the game.
     * @return The winner of the game.
     */
    public BlackJackPlayer findWinner() {
        if (player.getHandValue(true) > 21 && playerTwo.getHandValue(true) <= 21) {
            return playerTwo;
        } else if (playerTwo.getHandValue(true) > 21 && player.getHandValue(true) <= 21) {
            return player;
        } else if (player.getHandValue(true) > 21 && playerTwo.getHandValue(true) > 21) {
            return null;
        } else if (player.getHandValue(true) == playerTwo.getHandValue(true)) {
            return null;
        } else {
            int playerValue = player.getHandValue(true);
            int playerTwoValue = playerTwo.getHandValue(true);

            int playerDiff = 21 - playerValue;
            int playerTwoDiff = 21 - playerTwoValue;

            if (playerDiff < playerTwoDiff) {
                return player;
            } else if (playerDiff > playerTwoDiff) {
                return playerTwo;
            } else {
                return null;
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stopGame() {
        GameManager.removeGameSession(session);
    }
}
