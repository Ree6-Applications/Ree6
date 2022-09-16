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

@Command(name = "game", description = "Access Ree6 internal Games", category = Category.FUN)
public class Game implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply("This Command is only available as Slash Command!");
            return;
        }

        OptionMapping action = commandEvent.getSlashCommandInteractionEvent().getOption("action");
        OptionMapping game = commandEvent.getSlashCommandInteractionEvent().getOption("game");
        OptionMapping invite = commandEvent.getSlashCommandInteractionEvent().getOption("invite");

        if (action == null) {
            commandEvent.reply("You need to specify an action!");
            return;
        }

        switch (action.getAsString()) {
            case "create" -> {
                if (game == null) {
                    commandEvent.reply("Please specify a Game!");
                    return;
                }

                ArrayList<User> participants = new ArrayList<>();
                participants.add(commandEvent.getSlashCommandInteractionEvent().getUser());

                GamePlayer gamePlayer = new GamePlayer(commandEvent.getMember().getUser());
                gamePlayer.setInteractionHook(commandEvent.getInteractionHook());

                GameManager.createGameSession(RandomUtils.getRandomBase64String(7), game.getAsString(),
                        commandEvent.getChannel(), participants).getGame().joinGame(gamePlayer);
            }
            case "join" -> {
                if (invite == null) {
                    commandEvent.reply("Please specify an Invite!");
                    return;
                }

                GameSession gameSession = GameManager.getGameSession(invite.getAsString());
                gameSession.getParticipants().add(commandEvent.getMember().getUser());
                GamePlayer gamePlayer = new GamePlayer(commandEvent.getMember().getUser());
                gamePlayer.setInteractionHook(commandEvent.getInteractionHook());
                gameSession.getGame().joinGame(gamePlayer);
            }

            default -> commandEvent.reply("Unknown Action!");
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("game", "Access Ree6 internal Games")
                .addOption(OptionType.STRING, "action", "Either use create or join.", true)
                .addOption(OptionType.STRING, "game", "The Game you want to play.", false)
                .addOption(OptionType.STRING, "invite", "The Game Invite in case you want to join a Session.", false);
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}