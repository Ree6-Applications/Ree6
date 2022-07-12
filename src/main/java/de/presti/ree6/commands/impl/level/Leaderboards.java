package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to get the Leaderboard.
 */
@Command(name = "leaderboard", description = "Shows you the Rank Leaderboard of the current Server", category = Category.LEVEL)
public class Leaderboards implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getCommandManager().sendMessage("For the leaderboards of this Server visit:\n" +
                "voice leaderboard: <https://cp.ree6.de/leaderboard/voice?guildId=" + commandEvent.getGuild().getId() + ">\n" +
                "chat leaderboard: <https://cp.ree6.de/leaderboard/chat?guildId=" + commandEvent.getGuild().getId() + ">\n" +
                "Please noted that you have to be logged in to see these stats, reason for this is Discords Guidelines.\n" +
                "If you want to read more about this please visit: <https://support-dev.discord.com/hc/de/articles/360043053492-Statistics-Bot-Policy>",
                commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{ "lb" };
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }
}
