package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

/**
 * A command to get the Leaderboard.
 */
@Command(name = "leaderboard", description = "command.description.leaderboard", category = Category.LEVEL)
public class Leaderboards implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setContent(commandEvent.getResource("command.message.leaderboards"));
        messageCreateBuilder.addActionRow(
                Button.link("https://support-dev.discord.com/hc/de/articles/360043053492-Statistics-Bot-Policy", commandEvent.getResource("command.label.discordGuidelines")),
                Button.link("https://cp.ree6.de/leaderboard/chat?guildId=" + commandEvent.getGuild().getId(), commandEvent.getResource("command.label.chatLeaderboard")),
                Button.link("https://cp.ree6.de/leaderboard/voice?guildId=" + commandEvent.getGuild().getId(), commandEvent.getResource("command.label.voiceLeaderboard"))
        );
        commandEvent.reply(messageCreateBuilder.build());
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
