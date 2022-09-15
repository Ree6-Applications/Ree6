package de.presti.ree6.game.impl.blackjack;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.core.GameSession;
import de.presti.ree6.game.core.IGame;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class Blackjack implements IGame {

    private final GameSession session;
    BlackJackPlayer player, playerTwo;

    public Blackjack(GameSession gameSession) {
        session = gameSession;
        createGame();
    }

    @Override
    public void createGame() {
        if (session.getParticipants().isEmpty() || session.getParticipants().size() > 2) {
            Main.getInstance().getCommandManager().sendMessage("You need to have 2 participants to play this game!", 5, session.getChannel());
            stopGame();
        }
    }

    @Override
    public void startGame() {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Blackjack");
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setAuthor("**" + player.getRelatedUser().getAsTag() + "**", null, player.getRelatedUser().getAvatarUrl());
        embedBuilder.addField("**Your Cards**", "", true);
        embedBuilder.addField("**" + playerTwo.getRelatedUser().getAsTag() + "s Cards**", "", true);

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("game_blackjack_hit", "Hit"), Button.success("game_blackjack_stand", "Stand"), Button.secondary("game_blackjack_doubledown", "Double Down"));

        player.interactionHook.sendMessage(messageCreateBuilder.build()).queue();

        embedBuilder.setAuthor("**" + playerTwo.getRelatedUser().getAsTag() + "**", null, playerTwo.getRelatedUser().getAvatarUrl());
        embedBuilder.clearFields();
        embedBuilder.addField("**Your Cards**", "", true);
        embedBuilder.addField("**" + player.getRelatedUser().getAsTag() + "s Cards**", "", true);

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        playerTwo.interactionHook.sendMessage(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onReactionReceive(GenericMessageReactionEvent messageReactionEvent) {

    }

    @Override
    public void onMessageReceive(MessageReceivedEvent messageReceivedEvent) {

    }

    @Override
    public void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent) {
        switch (buttonInteractionEvent.getComponentId()) {
            case "game_blackjack_hit":
                break;
            case "game_blackjack_stand":
                break;
            case "game_blackjack_doubledown":
                break;
        }
    }

    @Override
    public void stopGame() {
        GameManager.removeGameSession(session);
    }
}
