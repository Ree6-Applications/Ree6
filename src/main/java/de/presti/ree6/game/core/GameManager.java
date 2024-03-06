package de.presti.ree6.game.core;

import de.presti.ree6.game.core.base.GameInfo;
import de.presti.ree6.game.core.base.IGame;
import de.presti.ree6.utils.others.RandomUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Class to manage Games.
 */
@Slf4j
public class GameManager {

    /**
     * A HashMap used to cache the Games.
     * The Key is the Name of the Game.
     */
    @Getter
    private final static HashMap<String, Class<? extends IGame>> gameCache = new HashMap<>();

    /**
     * A HashMap used to cache the GameSessions.
     * The Key is the ID of the Channel.
     */
    private final static HashMap<String, GameSession> gameSessions = new HashMap<>();

    /**
     * Should be called to load all Games into the cache.
     */
    public static void loadAllGames() {
        Reflections reflections = new Reflections("de.presti.ree6.game.impl");
        Set<Class<? extends IGame>> classes = reflections.getSubTypesOf(IGame.class);

        for (Class<? extends IGame> aClass : classes) {
            if (aClass.isAnnotationPresent(GameInfo.class)) {
                GameInfo gameInfo = aClass.getAnnotation(GameInfo.class);
                if (!gameCache.containsKey(gameInfo.name().trim().toLowerCase())) {
                    gameCache.put(gameInfo.name().trim().toLowerCase(), aClass);
                }
            }
        }
    }

    /**
     * Method used to create a new GameSession.
     *
     * @param gameIdentifier The Identifier of the Session.
     * @param gameName       The Name of the Game.
     * @param host           The Creator of the Game.
     * @param channel        The Channel where the Game is played.
     * @param participants   The Participants of the Game.
     * @return The created GameSession.
     */
    public static GameSession createGameSession(String gameIdentifier, String gameName, Member host, GuildMessageChannelUnion channel, ArrayList<User> participants) {
        GameSession gameSession = new GameSession(gameIdentifier, channel.getGuild(), host, channel, participants);
        gameSession.setGame(getGame(gameName, gameSession));
        gameSessions.put(gameIdentifier, gameSession);
        return gameSession;
    }

    /**
     * Generate a valid Invite
     *
     * @return the newly create Invite.
     */
    public static String generateInvite() {
        String key = RandomUtils.getRandomBase64String(4);
        if (gameSessions.containsKey(key)) {
            return generateInvite();
        }

        return key;
    }

    /**
     * Method used to get a Game by its name.
     *
     * @param gameName    The Name of the Game.
     * @param gameSession The GameSession of the Game.
     * @return The Game.
     */
    public static IGame getGame(String gameName, GameSession gameSession) {

        if (gameCache.containsKey(gameName.toLowerCase().trim())) {
            try {
                return gameCache.get(gameName.toLowerCase().trim()).getDeclaredConstructor(GameSession.class).newInstance(gameSession);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("Failed to create instance of " + gameName + "!", e);
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
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    log.error("Failed to create instance of " + aClass.getSimpleName() + "!", e);
                }
            }
        }

        return null;
    }

    /**
     * Method used to get a GameSession by its Identifier.
     *
     * @param gameIdentifier The Identifier of the GameSession.
     * @return The GameSession.
     */
    public static GameSession getGameSession(String gameIdentifier) {
        return gameSessions.get(gameIdentifier);
    }

    /**
     * Method used to get all GameSessions.
     *
     * @param channel The Channel where the GameSessions are played.
     * @return A List of GameSessions.
     */
    public static List<GameSession> getGameSessions(MessageChannelUnion channel) {
        return gameSessions.values().stream().filter(gameSession -> gameSession.getChannel().getId().equals(channel.getId())).toList();
    }

    /**
     * Method used to get all GameSessions.
     *
     * @return A List of GameSessions.
     */
    public static List<GameSession> getGameSessions() {
        return (List<GameSession>) gameSessions.values();
    }

    /**
     * Method used to remove a GameSession.
     *
     * @param session The GameSession.
     */
    public static void removeGameSession(GameSession session) {
        gameSessions.remove(session.getGameIdentifier());
    }

    /**
     * Method that returns every cache Game.
     *
     * @return A List of Games.
     */
    public static Collection<String> getGameNames() {
        return gameCache.keySet();
    }

    /**
     * Method that returns every cache Game.
     *
     * @return A List of Games.
     */
    public static Collection<Class<? extends IGame>> getGames() {
        return gameCache.values();
    }
}
