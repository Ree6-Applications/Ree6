package de.presti.ree6.commands.impl.economy;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.economy.MoneyHolder;
import de.presti.ree6.utils.data.EconomyUtil;
import de.presti.ree6.utils.others.RandomUtils;
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

        MoneyHolder targetHolder = EconomyUtil.getMoneyHolder(commandEvent.getGuild().getIdLong(), member.getIdLong(), false);

        // Leave them poor people alone ong.
        if (!EconomyUtil.hasCash(targetHolder) || targetHolder.getAmount() <= 50) {
            commandEvent.reply(commandEvent.getResource("message.steal.notEnoughMoney"), 5);
            return;
        }

        double stealAmount = targetHolder.getAmount() * RandomUtils.nextDouble(0.01, 0.25);
        if (EconomyUtil.pay(targetHolder, EconomyUtil.getMoneyHolder(commandEvent.getMember()), stealAmount, false, false)) {
            // TODO:: more variation in the messages.
            commandEvent.reply(commandEvent.getResource("message.steal.success", stealAmount), 5);
        } else {
            commandEvent.reply(commandEvent.getResource("message.steal.failed"), 5);
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
