package de.presti.ree6.commands.impl.economy;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.economy.MoneyHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Map;

@Command(name = "steal", description = "command.description.steal", category = Category.ECONOMY)
public class Steal implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        OptionMapping user = commandEvent.getOption("user");

        if (user == null) {
            commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
            return;
        }

        Member member = user.getAsMember();

        MoneyHolder targetHolder =
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new MoneyHolder(), "SELECT * FROM Money_Holder WHERE guildId = :gid AND userId = :uid",
                        Map.of("gid", commandEvent.getGuild().getId(), "uid", member.getIdLong()));

        if (targetHolder == null || targetHolder.getAmount() <= 0) {
            commandEvent.reply(commandEvent.getResource("message.steal.notEnoughMoney"), 5);
            return;
        }


    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
