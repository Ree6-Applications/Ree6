package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.*;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "leaderboard", description = "Shows you the Rank Leaderboard of the current Server", category = Category.LEVEL)
public class Leaderboards implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getCommandManager().sendMessage("For the Voice Leaderboard: <https://cp.ree6.de/leaderboard/voice?guildId=" + commandEvent.getGuild().getId() + ">\nAnd for the Chat Leaderboard: <https://cp.ree6.de/leaderboard/chat?guildId=" + commandEvent.getGuild().getId() + ">", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public String[] getAlias() {
        return new String[]{ "lb" };
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }
}
