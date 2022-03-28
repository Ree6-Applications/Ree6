package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;

public class Leaderboards extends CommandClass {

    public Leaderboards() {
        super("leaderboard", "Shows you the Rank Leaderboard", Category.LEVEL, new String[]{ "lb" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        sendMessage("For the Voice Leaderboard: <https://cp.ree6.de/leaderboard/voice?guildId=" + commandEvent.getGuild().getId() + ">\nAnd for the Chat Leaderboard: <https://cp.ree6.de/leaderboard/chat?guildId=" + commandEvent.getGuild().getId() + ">", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
