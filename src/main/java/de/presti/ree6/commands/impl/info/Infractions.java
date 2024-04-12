package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Warning;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Map;

/**
 * A command that allows users to see how many warnings/infractions they have.
 */
@Command(name = "infractions", description = "command.description.infractions", category = Category.INFO)
public class Infractions implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        OptionMapping userOption = commandEvent.getOption("user");
        Member member = userOption.getAsMember();

        if (member == null) {
            commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
            return;
        }

        Warning warning = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Warning(), "FROM Warning WHERE guildUserId.guildId = :gid AND guildUserId.userId = :uid", Map.of("gid", commandEvent.getGuild().getIdLong(), "uid", member.getIdLong()));

        if (warning != null) {
            commandEvent.reply(commandEvent.getResource("message.infractions.success", member.getAsMention(), warning.getWarnings()));
        } else {
            commandEvent.reply(commandEvent.getResource("message.infractions.empty", member.getAsMention()));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("infractions", "command.description.infractions")
                .addOption(OptionType.USER, "user", "The user you want to check up upon.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
