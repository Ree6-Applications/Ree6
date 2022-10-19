package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.core.GameSession;
import de.presti.ree6.game.core.base.GameInfo;
import de.presti.ree6.game.core.base.GamePlayer;
import de.presti.ree6.game.core.base.GameState;
import de.presti.ree6.language.LanguageService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.ArrayList;

/**
 * Command used to access the Game System.
 */
@Command(name = "game", description = "command.description.game", category = Category.FUN)
public class Game implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        OptionMapping action = commandEvent.getSlashCommandInteractionEvent().getOption("action");
        OptionMapping value = commandEvent.getSlashCommandInteractionEvent().getOption("value");

        if (action == null) {
            commandEvent.reply(commandEvent.getResource("command.game.actionNeeded"));
            return;
        }

        if (value == null) {
            commandEvent.reply(commandEvent.getResource("command.game.valueNeeded"));
            return;
        }

        switch (action.getAsString()) {
            case "create" -> {

                ArrayList<User> participants = new ArrayList<>();
                participants.add(commandEvent.getSlashCommandInteractionEvent().getUser());

                GamePlayer gamePlayer = new GamePlayer(commandEvent.getMember().getUser());
                gamePlayer.setInteractionHook(commandEvent.getInteractionHook());

                GameManager.createGameSession(GameManager.generateInvite(), value.getAsString(),
                        commandEvent.getChannel(), participants).getGame().joinGame(gamePlayer);
            }
            case "join" -> {

                GameSession gameSession = GameManager.getGameSession(value.getAsString());
                if (gameSession == null) {
                    commandEvent.reply(commandEvent.getResource("command.game.invalidInvite"));
                    return;
                }

                if (gameSession.getGameState() != GameState.WAITING) {
                    commandEvent.reply(commandEvent.getResource("command.game.gameAlreadyStarted"));
                    return;
                }

                gameSession.getParticipants().add(commandEvent.getMember().getUser());
                GamePlayer gamePlayer = new GamePlayer(commandEvent.getMember().getUser());
                gamePlayer.setInteractionHook(commandEvent.getInteractionHook());
                gameSession.getGame().joinGame(gamePlayer);
            }

            case "list" -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(commandEvent.getResource("command.game.availableGames")).append("```");
                GameManager.getGames().forEach(iGame -> stringBuilder.append("\n").append(iGame.getAnnotation(GameInfo.class).name()));
                stringBuilder.append("```");
                commandEvent.reply(stringBuilder.toString());
            }

            default -> commandEvent.reply(commandEvent.getResource("command.game.invalidAction"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("game", LanguageService.getDefault("command.description.game"))
                .addOption(OptionType.STRING, "action", "Either use create or join.", true)
                .addOption(OptionType.STRING, "value", "Either the Game name or Invite code.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}