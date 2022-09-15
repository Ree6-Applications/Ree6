package de.presti.ree6.game.core;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public interface IGame {

    void createGame();

    void startGame();

    void onReactionReceive(GenericMessageReactionEvent messageReactionEvent);

    void onMessageReceive(MessageReceivedEvent messageReceivedEvent);

    void stopGame();
}
