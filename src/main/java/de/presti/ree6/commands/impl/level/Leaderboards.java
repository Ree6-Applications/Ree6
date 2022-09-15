package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setContent("Below this Message you can find the Leaderboards of the current Server!\nPlease note that you have to be logged in to see these stats, reason for this is Discords Guidelines!");
        messageCreateBuilder.addActionRow(
                Button.link("https://support-dev.discord.com/hc/de/articles/360043053492-Statistics-Bot-Policy", "Discord Guidelines"),
                Button.link("https://cp.ree6.de/leaderboard/chat?guildId=" + commandEvent.getGuild().getId(), "Chat-Leaderboards"),
                Button.link("https://cp.ree6.de/leaderboard/voice?guildId=" + commandEvent.getGuild().getId(), "Voice-Leaderboards")
        );
        Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), commandEvent);
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
