package de.presti.ree6.game.core;

import de.presti.ree6.game.core.base.IGame;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.ArrayList;

/**
 * Class used to store Information about Games and its related Information.
 */
public class GameSession {

    /**
     * The Identifier of the GameSession.
     */
    final String gameIdentifier;

    /**
     * The Game class.
     */
    IGame game;

    /**
     * The Channel where the Game is played.
     */
    private final MessageChannelUnion channel;

    /**
     * The Participants of the Game.
     */
    ArrayList<User> participants;

    /**
     * Constructor for the GameSession.
     * @param gameIdentifier The Identifier of the GameSession.
     * @param messageChannelUnion The Channel where the Game is played.
     * @param participants The Participants of the Game.
     */
    public GameSession(String gameIdentifier, MessageChannelUnion messageChannelUnion, ArrayList<User> participants) {
        this(gameIdentifier, null, messageChannelUnion, participants);
    }

    /**
     * Constructor for the GameSession.
     * @param gameIdentifier The Identifier of the GameSession.
     * @param game The Game class.
     * @param messageChannelUnion The Channel where the Game is played.
     * @param participants The Participants of the Game.
     */
    public GameSession(String gameIdentifier, IGame game, MessageChannelUnion messageChannelUnion, ArrayList<User> participants) {
        this.gameIdentifier = gameIdentifier;
        this.game = game;
        this.channel = messageChannelUnion;
        this.participants = participants;
    }

    /**
     * Method used to get the Identifier of the GameSession.
     * @return The Identifier of the GameSession.
     */
    public String getGameIdentifier() {
        return gameIdentifier;
    }

    /**
     * Method used to get the Game class.
     * @return The Game class.
     */
    public IGame getGame() {
        return game;
    }

    /**
     * Method used to set the Game class.
     * @param game The Game class.
     */
    public void setGame(IGame game) {
        this.game = game;
    }

    /**
     * Method used to get the Channel where the Game is played.
     * @return The Channel where the Game is played.
     */
    public MessageChannelUnion getChannel() {
        return channel;
    }

    /**
     * Method used to get the Participants of the Game.
     * @return The Participants of the Game.
     */
    public ArrayList<User> getParticipants() {
        return participants;
    }
}
