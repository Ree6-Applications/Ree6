package de.presti.ree6.game.core;

import de.presti.ree6.game.core.base.GameState;
import de.presti.ree6.game.core.base.IGame;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
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
     * The current Game-State.
     */
    GameState gameState = GameState.WAITING;

    /**
     * The Channel where the Game is played.
     */
    private final GuildMessageChannelUnion channel;

    /**
     * The Guild in which this Session originated.
     */
    @Getter
    private final Guild guild;

    /**
     * The Creator of the Game.
     */
    @Getter
    private final Member host;

    /**
     * The Participants of the Game.
     */
    ArrayList<User> participants;

    /**
     * Constructor for the GameSession.
     *
     * @param gameIdentifier      The Identifier of the GameSession.
     * @param guild               The Guild in which this Session originated.
     * @param user                The Creator of the Game.
     * @param messageChannelUnion The Channel where the Game is played.
     * @param participants        The Participants of the Game.
     */
    public GameSession(String gameIdentifier, Guild guild, Member user, GuildMessageChannelUnion messageChannelUnion, ArrayList<User> participants) {
        this(gameIdentifier, guild, user, null, messageChannelUnion, participants);
    }

    /**
     * Constructor for the GameSession.
     *
     * @param gameIdentifier      The Identifier of the GameSession.
     * @param guild               The Guild in which this Session originated.
     * @param user                The Creator of the Game.
     * @param game                The Game class.
     * @param messageChannelUnion The Channel where the Game is played.
     * @param participants        The Participants of the Game.
     */
    public GameSession(String gameIdentifier, Guild guild, Member user, IGame game, GuildMessageChannelUnion messageChannelUnion, ArrayList<User> participants) {
        this.gameIdentifier = gameIdentifier;
        this.guild = guild;
        this.host = user;
        this.game = game;
        this.channel = messageChannelUnion;
        this.participants = participants;
    }

    /**
     * Method used to get the Identifier of the GameSession.
     *
     * @return The Identifier of the GameSession.
     */
    public String getGameIdentifier() {
        return gameIdentifier;
    }

    /**
     * Method used to get the Game class.
     *
     * @return The Game class.
     */
    public IGame getGame() {
        return game;
    }

    /**
     * Method used to set the Game class.
     *
     * @param game The Game class.
     */
    public void setGame(IGame game) {
        this.game = game;
    }

    /**
     * Method used to get the Channel where the Game is played.
     *
     * @return The Channel where the Game is played.
     */
    public GuildMessageChannelUnion getChannel() {
        return channel;
    }

    /**
     * Method used to get the Participants of the Game.
     *
     * @return The Participants of the Game.
     */
    public ArrayList<User> getParticipants() {
        return participants;
    }

    /**
     * Method used to get the current Game-State.
     *
     * @return The current Game-State.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Method used to set the current Game-State.
     *
     * @param gameState The current Game-State.
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Retrieve the current Session Host as {@link User}
     *
     * @return The current Session Host as {@link User}
     */
    public User getHostAsUser() {
        return host.getUser();
    }
}
