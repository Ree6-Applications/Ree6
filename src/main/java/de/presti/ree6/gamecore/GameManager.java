package de.presti.ree6.gamecore;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.HashMap;
import java.util.List;

public class GameManager {

    HashMap<String, GameSession> gameSessions = new HashMap<>();

    public void createGameSession(String gameIdentifier, IGame game, MessageChannelUnion channel, List<User> participants) {
        gameSessions.put(gameIdentifier, new GameSession(gameIdentifier, game, channel, participants));
    }

    public GameSession getGameSession(String gameIdentifier) {
        return gameSessions.get(gameIdentifier);
    }

}
