package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.core.GameSession;
import de.presti.ree6.game.core.base.GamePlayer;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.ArrayList;

/**
 * Command used to access the Game System.
 */
@Command(name = "game", description = "Access Ree6 internal Games", category = Category.FUN)
public class Game implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply("This Command is only available as Slash Command!");
            return;
        }

        OptionMapping action = commandEvent.getSlashCommandInteractionEvent().getOption("action");
        OptionMapping value = commandEvent.getSlashCommandInteractionEvent().getOption("value");

        if (action == null) {
            commandEvent.reply("You need to specify an action!");
            return;
        }

        if (value == null) {
            commandEvent.reply("Please specify a value!");
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
                    commandEvent.reply("This Invite is invalid!");
                    return;
                }

                gameSession.getParticipants().add(commandEvent.getMember().getUser());
                GamePlayer gamePlayer = new GamePlayer(commandEvent.getMember().getUser());
                gamePlayer.setInteractionHook(commandEvent.getInteractionHook());
                gameSession.getGame().joinGame(gamePlayer);
            }

            default -> commandEvent.reply("Unknown Action!");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("game", "Access Ree6 internal Games")
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