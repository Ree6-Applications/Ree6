package de.presti.ree6.game.core.base;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

/**
 * Interface for Games to implement.
 */
public interface IGame {

    /**
     * Called when the Game is created.
     */
    void createGame();

    /**
     * Called when the Game is started.
     */
    void startGame();

    /**
     * Called when a User wants to join the Game.
     * @param user The User who wants to join.
     */
    void joinGame(GamePlayer user);

    /**
     * Called when a User wants to leave the Game.
     * @param user The User who wants to leave.
     */
    void leaveGame(GamePlayer user);

    /**
     * Called when a Message is received.
     * @param messageReactionEvent The Event.
     */
    void onReactionReceive(GenericMessageReactionEvent messageReactionEvent);

    /**
     * Called when a Reaction is received.
     * @param messageReceivedEvent The Event.
     */
    void onMessageReceive(MessageReceivedEvent messageReceivedEvent);

    /**
     * Called when a Button is clicked.
     * @param buttonInteractionEvent The Event.
     */
    void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent);

    /**
     * Called when the Game is stopped.
     */
    void stopGame();
}
