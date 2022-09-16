package de.presti.ree6.game.core.base;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public interface IGame {

    void createGame();

    void startGame();

    void joinGame(GamePlayer user);

    void leaveGame(GamePlayer user);

    void onReactionReceive(GenericMessageReactionEvent messageReactionEvent);

    void onMessageReceive(MessageReceivedEvent messageReceivedEvent);

    void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent);

    void stopGame();
}
