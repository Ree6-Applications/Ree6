package de.presti.ree6.game.core;

import de.presti.ree6.game.core.base.IGame;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GameManager {

    private final static HashMap<String, GameSession> gameSessions = new HashMap<>();

    public static void createGameSession(String gameIdentifier, IGame game, MessageChannelUnion channel, List<User> participants) {
        gameSessions.put(gameIdentifier, new GameSession(gameIdentifier, game, channel, participants));
    }

    public static IGame getGame(String gameName) {
        Reflections reflections = new Reflections("de.presti.ree6.game.impl");
        Set<Class<? extends IGame>> classes = reflections.getSubTypesOf(IGame.class);

        for (Class<? extends IGame> aClass : classes) {
            if (aClass.getSimpleName().equalsIgnoreCase(gameName)) {
                try {
                    return (IGame) aClass.getConstructors()[0].newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    Main.getInstance().getLogger().error("Failed to create instance of " + aClass.getSimpleName() + "!", e);
                }
            }
        }

        return null;
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
