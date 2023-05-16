package de.presti.ree6.commands.impl.economy;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.economy.MoneyHolder;
import de.presti.ree6.utils.data.EconomyUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Map;

/**
 * Command to manage the player specific economy.
 */
@Command(name = "money", description = "command.description.money", category = Category.ECONOMY)
public class Money implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        OptionMapping amount = commandEvent.getOption("amount");
        OptionMapping user = commandEvent.getOption("user");

        String subcommand = commandEvent.getSubcommand();

        switch (subcommand) {
            case "withdraw" -> {
                if (amount == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                double withdrawAmount = amount.getAsDouble();

                MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());

                if (EconomyUtil.hasEnoughMoney(moneyHolder, withdrawAmount, true)) {
                    EconomyUtil.pay(moneyHolder, moneyHolder, withdrawAmount, true, false);
                    commandEvent.reply(commandEvent.getResource("message.money.withdraw", withdrawAmount), 5);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.money.notEnoughMoney"), 5);
                }
            }
            case "deposit" -> {
                if (amount == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                double depositAmount = amount.getAsDouble();

                MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());

                if (EconomyUtil.hasEnoughMoney(moneyHolder, depositAmount, false)) {
                    EconomyUtil.pay(moneyHolder, moneyHolder, depositAmount, false, true);
                    commandEvent.reply(commandEvent.getResource("message.money.deposit", depositAmount), 5);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.money.notEnoughMoney"), 5);
                }

                commandEvent.reply(commandEvent.getResource("message.money.deposit", depositAmount));
            }
            case "send" -> {
                if (user == null || amount == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                Member member = user.getAsMember();
                double sendAmount = amount.getAsDouble();

                MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());
                MoneyHolder target = EconomyUtil.getMoneyHolder(member);

                if (EconomyUtil.pay(moneyHolder, target, sendAmount, true, true)) {
                    commandEvent.reply(commandEvent.getResource("message.money.send", sendAmount, member.getAsMention()));
                } else {
                    commandEvent.reply(commandEvent.getResource("message.money.notEnoughMoney"), 5);
                }
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("money", "command.description.money")
                .addSubcommands(new SubcommandData("withdraw", "Withdraw your money from your bank into your pockets!")
                                .addOption(OptionType.NUMBER, "amount", "The amount of money you want to withdraw.", true),
                        new SubcommandData("deposit", "Deposit your money from your pockets into your bank!")
                                .addOption(OptionType.NUMBER, "amount", "The amount of money you want to deposit", true),
                        new SubcommandData("send", "Send money to another user!")
                                .addOption(OptionType.USER, "user", "The user you want to send money to.", true)
                                .addOption(OptionType.NUMBER, "amount", "The amount of money you want to pay.", true));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
