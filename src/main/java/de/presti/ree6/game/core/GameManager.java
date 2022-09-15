package de.presti.ree6.game.core;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.HashMap;
import java.util.List;

public class GameManager {

    private final static HashMap<String, GameSession> gameSessions = new HashMap<>();

    public static void createGameSession(String gameIdentifier, IGame game, MessageChannelUnion channel, List<User> participants) {
        gameSessions.put(gameIdentifier, new GameSession(gameIdentifier, game, channel, participants));
    }

    public static GameSession getGameSession(String gameIdentifier) {
        return gameSessions.get(gameIdentifier);
    }

    public static List<GameSession> getGameSessions(MessageChannelUnion channel) {
        return gameSessions.values().stream().filter(gameSession -> gameSession.getChannel().getId().equals(channel.getId())).toList();
    }

    public static List<GameSession> getGameSessions() {
        return (List<GameSession>) gameSessions.values();
    }

    public static void removeGameSession(GameSession session) {
        gameSessions.remove(session.getGameIdentifier());
    }
}
