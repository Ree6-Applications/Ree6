package de.presti.ree6.game.core;

import de.presti.ree6.game.core.base.GameInfo;
import de.presti.ree6.game.core.base.GamePlayer;
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

    private final static HashMap<String, Class<? extends IGame>> gameCache = new HashMap<>();

    private final static HashMap<String, GameSession> gameSessions = new HashMap<>();

    public static void createGameSession(String gameIdentifier, String gameName, MessageChannelUnion channel, List<User> participants) {
        GameSession gameSession = new GameSession(gameIdentifier, channel, participants);
        gameSession.setGame(getGame(gameName, gameSession));
        gameSession.getGame().joinGame(new GamePlayer(participants.get(0)));
        gameSessions.put(gameIdentifier, gameSession);
    }

    public static IGame getGame(String gameName, GameSession gameSession) {

        if (gameCache.containsKey(gameName.toLowerCase().trim())) {
            try {
                return gameCache.get(gameName.toLowerCase().trim()).getDeclaredConstructor(GameSession.class).newInstance(gameSession);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
                Main.getInstance().getLogger().error("Failed to create instance of " + gameName + "!", e);
            }
        }

        Reflections reflections = new Reflections("de.presti.ree6.game.impl");
        Set<Class<? extends IGame>> classes = reflections.getSubTypesOf(IGame.class);

        for (Class<? extends IGame> aClass : classes) {
            if (aClass.isAnnotationPresent(GameInfo.class) && aClass.getAnnotation(GameInfo.class).name().trim().equalsIgnoreCase(gameName)) {
                try {
                    if (!gameCache.containsKey(gameName.toLowerCase().trim())) {
                        gameCache.put(gameName.toLowerCase().trim(), aClass);
                    }
                    return aClass.getDeclaredConstructor(GameSession.class).newInstance(gameSession);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
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
