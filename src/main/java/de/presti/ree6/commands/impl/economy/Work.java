package de.presti.ree6.commands.impl.economy;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.utils.data.EconomyUtil;
import de.presti.ree6.utils.others.RandomUtils;
import de.presti.ree6.utils.others.ThreadUtil;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

/**
 * Work for money.
 */
@Command(name = "work", description = "command.description.work", category = Category.ECONOMY)
public class Work implements ICommand {

    /**
     * List of every User that is on cooldown.
     */
    ArrayList<String> workTimeout = new ArrayList<>();

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        String entryString = commandEvent.getGuild().getIdLong() + "-" + commandEvent.getMember().getIdLong();

        long delay = Long.parseLong((String) SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "FROM Setting WHERE settingId.guildId=:gid AND settingId.name=:name",
                Map.of("gid", commandEvent.getGuild().getId(), "name", "configuration_work_delay")).getValue());

        if (workTimeout.contains(entryString)) {
            commandEvent.reply(commandEvent.getResource("message.work.cooldown", delay));
            return;
        }

        double min = Double.parseDouble((String) SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "FROM Setting WHERE settingId.guildId=:gid AND settingId.name=:name",
                Map.of("gid", commandEvent.getGuild().getId(), "name", "configuration_work_min")).getValue());

        double max = Double.parseDouble((String) SQLSession.getSqlConnector().getSqlWorker().getEntity(new Setting(), "FROM Setting WHERE settingId.guildId=:gid AND settingId.name=:name",
                Map.of("gid", commandEvent.getGuild().getId(), "name", "configuration_work_max")).getValue());

        double amount = RandomUtils.round(RandomUtils.nextDouble(min, max), 2);

        if (EconomyUtil.pay(null, EconomyUtil.getMoneyHolder(commandEvent.getMember()), amount, false, false, true)) {
            // TODO:: add more variation messages.
            commandEvent.reply(commandEvent.getResource("message.work.success", EconomyUtil.formatMoney(amount)));
        } else {
            commandEvent.reply(commandEvent.getResource("message.work.fail"));
        }

        workTimeout.add(entryString);
        ThreadUtil.createThread(x -> workTimeout.remove(entryString), Duration.ofSeconds(delay), false, false);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
