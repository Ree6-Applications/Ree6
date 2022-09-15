package de.presti.ree6.events;

import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.core.GameSession;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GameEvents extends ListenerAdapter {

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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("game_start")) {
            if (event.getComponentId().contains(":")) {
                String gameIdentifier = event.getComponentId().split(":")[1];
                GameSession gameSession = GameManager.getGameSession(gameIdentifier);
                if (gameSession != null) {
                    if (gameSession.getChannel().getId().equals(event.getChannel().getId())) {
                        if (gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getUser().getId()))) {
                            gameSession.getGame().startGame();
                        }
                    }
                }
            }
        }
        for (GameSession gameSession : GameManager.getGameSessions(event.getChannel())) {
            if (gameSession.getChannel().getId().equals(event.getChannel().getId())) {
                if (gameSession.getParticipants().stream().anyMatch(user -> user.getId().equals(event.getUser().getId()))) {
                    gameSession.getGame().onButtonInteractionReceive(event);
                }
            }
        }
    }
}
