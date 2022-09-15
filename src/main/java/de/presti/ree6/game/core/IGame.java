package de.presti.ree6.game.core;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public interface IGame {

    void createGame();

    void startGame();

    void joinGame(User user);

    void leaveGame(User user);

    void onReactionReceive(GenericMessageReactionEvent messageReactionEvent);

    void onMessageReceive(MessageReceivedEvent messageReceivedEvent);

    void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent);

    void stopGame();
}
