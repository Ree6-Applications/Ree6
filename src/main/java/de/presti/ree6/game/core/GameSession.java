package de.presti.ree6.game.core;

import de.presti.ree6.game.core.base.IGame;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.List;

public class GameSession {

    final String gameIdentifier;
    IGame game;
    private final MessageChannelUnion channel;
    List<User> participants;

    public GameSession(String gameIdentifier, MessageChannelUnion messageChannelUnion, List<User> participants) {
        this(gameIdentifier, null, messageChannelUnion, participants);
    }

    public GameSession(String gameIdentifier, IGame game, MessageChannelUnion messageChannelUnion, List<User> participants) {
        this.gameIdentifier = gameIdentifier;
        this.game = game;
        this.channel = messageChannelUnion;
        this.participants = participants;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
    }
    public IGame getGame() {
        return game;
    }
    public void setGame(IGame game) {
        this.game = game;
    }
    public MessageChannelUnion getChannel() {
        return channel;
    }
    public List<User> getParticipants() {
        return participants;
    }
}
