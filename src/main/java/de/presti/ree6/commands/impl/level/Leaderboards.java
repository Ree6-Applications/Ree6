package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import de.presti.ree6.bot.BotConfig;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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
        messageCreateBuilder.setContent(commandEvent.getResource("message.leaderboards"));
        messageCreateBuilder.setComponents(ActionRow.of(
                Button.link("https://support-dev.discord.com/hc/de/articles/360043053492-Statistics-Bot-Policy", commandEvent.getResource("label.discordGuidelines")),
                Button.link(BotConfig.getWebinterface() + "/dash/" + commandEvent.getGuild().getId() + "/leaderboards", commandEvent.getResource("label.chatLeaderboard")),
                Button.link(BotConfig.getWebinterface() + "/dash/" + commandEvent.getGuild().getId() + "/leaderboards", commandEvent.getResource("label.voiceLeaderboard"))
        ));
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
