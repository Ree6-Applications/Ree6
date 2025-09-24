package de.presti.ree6.module.game.impl.blackjack;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.game.core.GameManager;
import de.presti.ree6.module.game.core.GameSession;
import de.presti.ree6.module.game.core.base.GameInfo;
import de.presti.ree6.module.game.core.base.GamePlayer;
import de.presti.ree6.module.game.core.base.GameState;
import de.presti.ree6.module.game.core.base.IGame;
import de.presti.ree6.module.game.impl.blackjack.entities.BlackJackCard;
import de.presti.ree6.module.game.impl.blackjack.entities.BlackJackPlayer;
import de.presti.ree6.module.game.impl.blackjack.util.BlackJackCardUtility;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.awt.*;
import java.util.ArrayList;

/**
 * Class used to represent the game of blackjack.
 */
@GameInfo(name = "Blackjack", description = "game.description.blackjack", minPlayers = 2, maxPlayers = 2)
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
     *
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
     * @see IGame#createGame()
     */
    @Override
    public void createGame() {
        if (session.getParticipants().isEmpty() || session.getParticipants().size() > 2) {
            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.gameCore.needMore", 2).block(), session.getChannel());
            stopGame();
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack").block());
        embedBuilder.setColor(BotConfig.getMainColor());
        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.welcome", session.getGameIdentifier()).block());

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setComponents(ActionRow.of(Button.primary("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame").block()).asDisabled(),
                Button.secondary("game_join:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.joinGame").block()).asEnabled()));
        session.getChannel().sendMessage(messageCreateBuilder.build()).queue(message -> menuMessage = message);
    }

    /**
     * @see IGame#startGame()
     */
    @Override
    public void startGame() {
        if (session.getGameState() == GameState.STARTED) {
            return;
        }

        session.setGameState(GameState.STARTED);

        BlackJackCard card = getRandomCard();

        card.setHidden(false);
        player.getHand().add(card);
        player.getHand().add(getRandomCard());

        BlackJackCard card2 = getRandomCard();
        card2.setHidden(false);
        playerTwo.getHand().add(card2);
        playerTwo.getHand().add(getRandomCard());

        updateViews(player, playerTwo);
        Main.getInstance().getCommandManager().deleteMessageWithoutException(menuMessage, null);
        currentPlayer = player;
    }

    /**
     * @see IGame#joinGame(GamePlayer)
     */
    @Override
    public void joinGame(GamePlayer user) {
        if (session.getGameState() == GameState.STARTED) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.alreadyStarted").block()).queue();
            return;
        }

        if (player != null && playerTwo != null) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.full").block()).queue();
            return;
        }

        if ((player != null && user.getRelatedUserId() == player.getRelatedUserId()) || (playerTwo != null && user.getRelatedUserId() == playerTwo.getRelatedUserId())) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.alreadyIn").block()).queue();
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
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.minimalReached").block());
            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setComponents(ActionRow.of(Button.success("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame").block()).asEnabled()));
            menuMessage.editMessage(messageEditBuilder.build()).queue();

            messageEditBuilder.clear();
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.joined").block());
            messageEditBuilder.setEmbeds(embedBuilder.build());
            playerTwo.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

            messageEditBuilder.clear();
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.minimalReachedHost").block());
            messageEditBuilder.setEmbeds(embedBuilder.build());
            player.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
        }
    }

    /**
     * @see IGame#leaveGame(GamePlayer)
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
     * @see IGame#onButtonInteractionReceive(ButtonInteractionEvent)
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
                buttonInteractionEvent.deferEdit().queue();
                if (player.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    hit(player, playerTwo);
                } else if (playerTwo.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    hit(playerTwo, player);
                }
            }

            case "game_blackjack_stand" -> {
                buttonInteractionEvent.deferEdit().queue();
                if (player.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    stand(player, playerTwo);
                } else if (playerTwo.getRelatedUserId() == buttonInteractionEvent.getUser().getIdLong()) {
                    stand(playerTwo, player);
                }
            }

            default -> {
                //buttonInteractionEvent.deferEdit().queue();
                //buttonInteractionEvent.editMessage(LanguageService.getByInteraction(player.getInteractionHook().getInteraction(), "message.default.invalidQuery")).queue();
            }
        }
    }

    /**
     * Get a random card from the basic Deck, but check if the card is already a players hand.
     *
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
     *
     * @param currentPlayer The player who hit.
     * @param nextPlayer    The other player.
     */
    public void hit(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        standUsed = false;

        BlackJackCard card = getRandomCard();

        if (card == null) {
            stopGame();
            return;
        }

        currentPlayer.getHand().add(card);

        updateViews(currentPlayer, nextPlayer, false);

        if (currentPlayer.getHandValue(true) > 21) {
            stopGame(currentPlayer, nextPlayer);
        }
    }

    /**
     * Stand the player and let the other player play.
     *
     * @param currentPlayer The player who stands.
     * @param nextPlayer    The other player.
     */
    public void stand(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {

        if (standUsed) {
            stopGame(currentPlayer, nextPlayer);
        } else {
            standUsed = true;
        }

        updateViews(nextPlayer, currentPlayer);
        this.currentPlayer = nextPlayer;
    }

    /**
     * Stop the game and check who won.
     *
     * @param currentPlayer The player who won.
     * @param nextPlayer    The other player.
     */
    public void stopGame(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack").block());
        embedBuilder.setColor(BotConfig.getMainColor());
        BlackJackPlayer winner = findWinner();

        if (winner == null) {
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.end.draw").block());
        } else {
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.end.win", winner.getRelatedUser().getAsMention(), winner.getHandValue(true)).block());
        }

        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", currentPlayer.getRelatedUser().getEffectiveName()).block(), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand", currentPlayer.getHandAsString(true), currentPlayer.getHandValue(true)).block(), true);
        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", nextPlayer.getRelatedUser().getEffectiveName()).block(), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand", nextPlayer.getHandAsString(true), nextPlayer.getHandValue(true)).block(), true);

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setComponents(new ArrayList<>());
        currentPlayer.getInteractionHook().editOriginalComponents(new ArrayList<>()).queue();
        nextPlayer.getInteractionHook().editOriginalComponents(new ArrayList<>()).queue();

        Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), session.getChannel());
        SQLSession.getSqlConnector().getSqlWorker().getSetting(session.getGuild().getIdLong(),
                "configuration_rewards_blackjack_win")
                .subscribe(setting -> rewardPlayer(session, winner, setting.get().getValue()));

        stopGame();
    }

    /**
     * Called to update the current Views of all Players.
     *
     * @param currentPlayer The current player.
     * @param nextPlayer    The next player.
     */
    public void updateViews(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        updateViews(currentPlayer, nextPlayer, true);
    }

    /**
     * Called to update the current Views of all Players.
     *
     * @param currentPlayer The current player.
     * @param nextPlayer    The next player.
     * @param addButtons    If the buttons should be added.
     */
    public void updateViews(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer, boolean addButtons) {
        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();

        EmbedBuilder currentPlayerEmbed = new EmbedBuilder();
        currentPlayerEmbed.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack").block());
        currentPlayerEmbed.setColor(BotConfig.getMainColor());
        currentPlayerEmbed.setAuthor(currentPlayer.getRelatedUser().getEffectiveName(), null, currentPlayer.getRelatedUser().getEffectiveAvatarUrl());
        currentPlayerEmbed.addField(LanguageService.getByGuild(session.getGuild(), "label.userCardsSelf").block(), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand",
                currentPlayer.getHandAsString(true), currentPlayer.getHandValue(true)).block(), true);
        currentPlayerEmbed.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", nextPlayer.getRelatedUser().getEffectiveName()).block(), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand",
                nextPlayer.getHandAsString(false), nextPlayer.getHandValue(false)).block(), true);

        currentPlayerEmbed.setFooter(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.turn.player").block());

        messageEditBuilder.setEmbeds(currentPlayerEmbed.build());

        if (addButtons) {
            messageEditBuilder.setComponents(ActionRow.of(Button.primary("game_blackjack_hit", LanguageService.getByGuild(session.getGuild(), "label.hit").block()),
                    Button.success("game_blackjack_stand", LanguageService.getByGuild(session.getGuild(), "label.stand").block())));
        }

        currentPlayer.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

        EmbedBuilder nextPlayerEmbed = new EmbedBuilder();

        nextPlayerEmbed.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack").block());
        nextPlayerEmbed.setColor(Color.RED);
        nextPlayerEmbed.setAuthor(nextPlayer.getRelatedUser().getEffectiveName(), null, nextPlayer.getRelatedUser().getEffectiveAvatarUrl());
        nextPlayerEmbed.addField(LanguageService.getByGuild(session.getGuild(), "label.userCardsSelf").block(), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand", nextPlayer.getHandAsString(true), nextPlayer.getHandValue(true)).block(), true);
        nextPlayerEmbed.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", currentPlayer.getRelatedUser().getEffectiveName()).block(), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand", currentPlayer.getHandAsString(false), currentPlayer.getHandValue(false)).block(), true);
        nextPlayerEmbed.setFooter(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.turn.wait").block());
        messageEditBuilder.setEmbeds(nextPlayerEmbed.build());

        nextPlayer.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
    }

    /**
     * Determine the winner of the game.
     *
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
     * @see IGame#stopGame()
     */
    @Override
    public void stopGame() {
        GameManager.removeGameSession(session);
    }
}
