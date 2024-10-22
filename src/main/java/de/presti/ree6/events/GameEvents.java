package de.presti.ree6.events;

import de.presti.ree6.module.game.core.GameManager;
import de.presti.ree6.module.game.core.GameSession;
import de.presti.ree6.module.game.core.base.GamePlayer;
import de.presti.ree6.module.game.core.base.GameState;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for Game Events.
 */
public class GameEvents extends ListenerAdapter {

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (GameSession gameSession : GameManager.getGameSessions(event.getChannel())) {
            if (gameSession.getChannel().getId().equals(event.getChannel().getId())) {
                if (gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getAuthor().getId()))) {
                    gameSession.getGame().onMessageReceive(event);
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {
        for (GameSession gameSession : GameManager.getGameSessions(event.getChannel())) {
            if (gameSession.getChannel().getId().equals(event.getChannel().getId())) {
                if (gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getUserId()))) {
                    gameSession.getGame().onReactionReceive(event);
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("game_start") &&
                event.getComponentId().contains(":")) {
            String gameIdentifier = event.getComponentId().split(":")[1];
            GameSession gameSession = GameManager.getGameSession(gameIdentifier);
            if (gameSession != null && gameSession.getGameState() == GameState.WAITING &&
                    gameSession.getChannel().getId().equals(event.getChannel().getId()) &&
                    gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getUser().getId()))) {
                gameSession.getGame().startGame();
            }
        }

        if (event.getComponentId().startsWith("game_join") &&
                event.getComponentId().contains(":")) {
            String gameIdentifier = event.getComponentId().split(":")[1];
            GameSession gameSession = GameManager.getGameSession(gameIdentifier);
            if (gameSession != null && gameSession.getGameState() == GameState.WAITING &&
                    gameSession.getChannel().getId().equals(event.getChannel().getId()) &&
                    gameSession.getParticipants().stream().noneMatch(user -> user.getId().equals(event.getUser().getId()))) {
                event.deferReply(true).queue(interactionHook -> {
                    GamePlayer gamePlayer = new GamePlayer(event.getUser());
                    gamePlayer.setInteractionHook(interactionHook);
                    gameSession.getParticipants().add(event.getUser());
                    gameSession.getGame().joinGame(gamePlayer);
                });
            }
        }

        if (event.getComponentId().startsWith("game_leave") &&
                event.getComponentId().contains(":")) {
            String gameIdentifier = event.getComponentId().split(":")[1];
            GameSession gameSession = GameManager.getGameSession(gameIdentifier);
            if (gameSession != null && gameSession.getGameState() == GameState.WAITING &&
                    gameSession.getChannel().getId().equals(event.getChannel().getId()) &&
                    gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getUser().getId()))) {
                event.deferReply(true).queue(interactionHook -> {
                    GamePlayer gamePlayer = new GamePlayer(event.getUser());
                    gamePlayer.setInteractionHook(interactionHook);
                    gameSession.getGame().leaveGame(gamePlayer);
                });
            }
        }

        for (GameSession gameSession : GameManager.getGameSessions(event.getChannel())) {
            if (gameSession.getChannel().getId().equals(event.getChannel().getId()) &&
                    gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getUser().getId()))) {
                gameSession.getGame().onButtonInteractionReceive(event);
            }
        }
    }
}
