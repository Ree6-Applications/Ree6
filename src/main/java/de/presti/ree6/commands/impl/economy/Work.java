package de.presti.ree6.commands.impl.economy;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.entities.economy.MoneyHolder;
import de.presti.ree6.utils.data.EconomyUtil;
import de.presti.ree6.utils.others.RandomUtils;
import de.presti.ree6.utils.others.ThreadUtil;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

@Command(name = "work", description = "command.description.work", category = Category.ECONOMY)
public class Work implements ICommand {

    ArrayList<String> workTimeout = new ArrayList<>();

    @Override
    public void onPerform(CommandEvent commandEvent) {
        String entryString = commandEvent.getGuild().getIdLong() + "-" + commandEvent.getMember().getIdLong();

        long delay = (long) SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "SELECT * FROM Settings WHERE GID=:gid AND NAME=:name",
                Map.of("gid", commandEvent.getGuild().getIdLong(), "name", "configuration_work_delay")).getValue();

        if (workTimeout.contains(entryString)) {
            commandEvent.reply(commandEvent.getResource("message.work.timeout"));
            return;
        }

        double min = (double) SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "SELECT * FROM Settings WHERE GID=:gid AND NAME=:name",
                Map.of("gid", commandEvent.getGuild().getIdLong(), "name", "configuration_work_min")).getValue();

        double max = (double) SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "SELECT * FROM Settings WHERE GID=:gid AND NAME=:name",
                Map.of("gid", commandEvent.getGuild().getIdLong(), "name", "configuration_work_max")).getValue();

        double amount = RandomUtils.nextDouble(min, max);

        if (EconomyUtil.pay(null, EconomyUtil.getMoneyHolder(commandEvent.getMember()), amount, false, false, true)) {
            // TODO:: add more variation messages.
            commandEvent.reply(commandEvent.getResource("message.work.success", amount));

            ThreadUtil.createThread(x -> workTimeout.remove(entryString), Duration.ofSeconds(delay), false, false);
        } else {
            commandEvent.reply(commandEvent.getResource("message.work.fail"));
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
