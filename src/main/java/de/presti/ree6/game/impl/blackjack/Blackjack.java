package de.presti.ree6.game.impl.blackjack;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.core.GameSession;
import de.presti.ree6.game.core.base.GameInfo;
import de.presti.ree6.game.core.base.GamePlayer;
import de.presti.ree6.game.core.base.GameState;
import de.presti.ree6.game.core.base.IGame;
import de.presti.ree6.game.impl.blackjack.entities.BlackJackCard;
import de.presti.ree6.game.impl.blackjack.entities.BlackJackPlayer;
import de.presti.ree6.game.impl.blackjack.util.BlackJackCardUtility;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.ArrayList;
import java.util.Map;

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
            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.gameCore.needMore", 2), session.getChannel());
            stopGame();
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack"));
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.welcome", session.getGameIdentifier()));

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame")).asDisabled(),
                Button.secondary("game_join:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.joinGame")).asEnabled());
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
        Main.getInstance().getCommandManager().deleteMessage(menuMessage, null);
        currentPlayer = player;
    }

    /**
     * @see IGame#joinGame(GamePlayer)
     */
    @Override
    public void joinGame(GamePlayer user) {
        if (session.getGameState() == GameState.STARTED) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.alreadyStarted")).queue();
            return;
        }

        if (player != null && playerTwo != null) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.full")).queue();
            return;
        }

        if ((player != null && user.getRelatedUserId() == player.getRelatedUserId()) || (playerTwo != null && user.getRelatedUserId() == playerTwo.getRelatedUserId())) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.alreadyIn")).queue();
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
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.minimalReached"));
            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setActionRow(Button.success("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame")).asEnabled());
            menuMessage.editMessage(messageEditBuilder.build()).queue();

            messageEditBuilder.clear();
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.joined"));
            messageEditBuilder.setEmbeds(embedBuilder.build());
            playerTwo.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

            messageEditBuilder.clear();
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.minimalReachedHost"));
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

            default -> {
                buttonInteractionEvent.deferEdit().queue();
                buttonInteractionEvent.editMessage(LanguageService.getByInteraction(player.getInteractionHook().getInteraction(), "message.default.invalidQuery")).queue();
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
     * @param currentPlayer The player who hit.
     * @param nextPlayer The other player.
     */
    public void hit(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        standUsed = false;

        BlackJackCard card = getRandomCard();

        if (card == null) {
            stopGame();
            return;
        }

        currentPlayer.getHand().add(card);

        if (currentPlayer.getHandValue(true) > 21) {
            stopGame(currentPlayer, nextPlayer);
        } else {
            this.currentPlayer = nextPlayer;
        }
    }

    /**
     * Stand the player and let the other player play.
     * @param currentPlayer The player who stand.
     * @param nextPlayer The other player.
     */
    public void stand(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        if (standUsed) {
            stopGame(currentPlayer, nextPlayer);
        } else {
            standUsed = true;
        }

        updateViews(currentPlayer, nextPlayer);
        this.currentPlayer = nextPlayer;
    }

    /**
     * Stop the game and check who won.
     * @param currentPlayer The player who won.
     * @param nextPlayer The other player.
     */
    public void stopGame(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack"));
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        BlackJackPlayer winner = findWinner();

        if (winner == null) {
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.end.draw"));
        } else {
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.end.win", winner.getRelatedUser().getAsMention(), winner.getHandValue(true)));
        }

        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", currentPlayer.getRelatedUser().getAsTag()), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand", currentPlayer.getHandAsString(true), currentPlayer.getHandValue(true)), true);
        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", nextPlayer.getRelatedUser().getAsTag()), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand", nextPlayer.getHandAsString(true), nextPlayer.getHandValue(true)), true);

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setComponents(new ArrayList<>());
        currentPlayer.getInteractionHook().editOriginalComponents(new ArrayList<>()).queue();
        nextPlayer.getInteractionHook().editOriginalComponents(new ArrayList<>()).queue();

        Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), session.getChannel());
        rewardPlayer(session, winner, SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "FROM Setting WHERE settingId.guildId=:gid AND settingId.name=:name",
                Map.of("gid", session.getGuild().getIdLong(), "name", "configuration_rewards_blackjack_win")).getValue());
        stopGame();
    }

    /**
     * Called to update the current Views of all Players.
     * @param currentPlayer The current player.
     * @param nextPlayer The next player.
     */
    public void updateViews(BlackJackPlayer currentPlayer, BlackJackPlayer nextPlayer) {
        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.blackJack"));
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setAuthor(currentPlayer.getRelatedUser().getAsTag(), null, currentPlayer.getRelatedUser().getEffectiveAvatarUrl());
        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCardsSelf"), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand",currentPlayer.getHandAsString(true), currentPlayer.getHandValue(true)), true);
        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", nextPlayer.getRelatedUser().getAsTag()), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand",nextPlayer.getHandAsString(false), nextPlayer.getHandValue(false)), true);

        embedBuilder.setFooter(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.turn.wait"));

        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.primary("game_blackjack_hit", LanguageService.getByGuild(session.getGuild(), "label.hit")), Button.success("game_blackjack_stand", LanguageService.getByGuild(session.getGuild(), "label.stand")));

        currentPlayer.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

        embedBuilder.setAuthor(nextPlayer.getRelatedUser().getAsTag(), null, nextPlayer.getRelatedUser().getEffectiveAvatarUrl());
        embedBuilder.clearFields();
        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCardsSelf"), LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.playerHand",nextPlayer.getHandAsString(true), nextPlayer.getHandValue(true)), true);
        embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.userCards", currentPlayer.getRelatedUser().getAsTag()), LanguageService.getByGuild(session.getGuild(),"message.blackJackGame.playerHand", currentPlayer.getHandAsString(false), currentPlayer.getHandValue(false)), true);
        embedBuilder.setFooter(LanguageService.getByGuild(session.getGuild(), "message.blackJackGame.turn.player"));
        messageEditBuilder.setEmbeds(embedBuilder.build());
        nextPlayer.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();
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
     * @see IGame#stopGame()
     */
    @Override
    public void stopGame() {
        GameManager.removeGameSession(session);
    }
}
