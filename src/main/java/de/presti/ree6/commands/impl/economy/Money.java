package de.presti.ree6.commands.impl.economy;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.economy.MoneyHolder;
import de.presti.ree6.utils.data.EconomyUtil;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

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
        String subcommandGroup = commandEvent.getSubcommandGroup();

        if (subcommandGroup.isBlank()) {
            switch (subcommand) {
                case "withdraw" -> {
                    if (amount == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    double withdrawAmount = RandomUtils.round(amount.getAsDouble(), 2);

                    MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());

                    if (EconomyUtil.hasEnoughMoney(moneyHolder, withdrawAmount, true)) {
                        EconomyUtil.pay(moneyHolder, moneyHolder, withdrawAmount, true, false);
                        commandEvent.reply(commandEvent.getResource("message.money.withdraw", EconomyUtil.formatMoney(withdrawAmount)), 5);
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.money.notEnoughMoney"), 5);
                    }
                }
                case "deposit" -> {
                    if (amount == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    double depositAmount = RandomUtils.round(amount.getAsDouble(), 2);

                    MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());

                    if (EconomyUtil.hasEnoughMoney(moneyHolder, depositAmount, false)) {
                        EconomyUtil.pay(moneyHolder, moneyHolder, depositAmount, false, true);
                        commandEvent.reply(commandEvent.getResource("message.money.deposit", EconomyUtil.formatMoney(depositAmount)), 5);
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.money.notEnoughMoney"), 5);
                    }
                }
                case "send" -> {
                    if (user == null || amount == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    Member member = user.getAsMember();

                    if (member == null) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                        return;
                    }

                    double sendAmount = RandomUtils.round(amount.getAsDouble(), 2);

                    MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());
                    MoneyHolder target = EconomyUtil.getMoneyHolder(member);

                    if (EconomyUtil.pay(moneyHolder, target, sendAmount, true, true)) {
                        commandEvent.reply(commandEvent.getResource("message.money.send", EconomyUtil.formatMoney(sendAmount), member.getAsMention()));
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.money.notEnoughMoney"), 5);
                    }
                }
                default -> {
                    if (user != null) {
                        Member member = user.getAsMember();

                        if (member == null) {
                            commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                            return;
                        }

                        MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(member);
                        commandEvent.reply(commandEvent.getResource("message.money.balance", member.getAsMention(), EconomyUtil.formatMoney(moneyHolder.getAmount()), EconomyUtil.formatMoney(moneyHolder.getBankAmount())));
                    } else {
                        MoneyHolder moneyHolder = EconomyUtil.getMoneyHolder(commandEvent.getMember());
                        commandEvent.reply(commandEvent.getResource("message.money.balance", commandEvent.getMember().getAsMention(), EconomyUtil.formatMoney(moneyHolder.getAmount()), EconomyUtil.formatMoney(moneyHolder.getBankAmount())));
                    }
                }
            }
        } else {
            if (!commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", "ADMINISTRATOR"));
                return;
            }

            if (user == null || amount == null) {
                commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                return;
            }

            OptionMapping bankOption = commandEvent.getOption("bank");

            boolean transferToBank = bankOption != null && bankOption.getAsBoolean();

            Member member = user.getAsMember();

            if (member == null) {
                commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                return;
            }

            double optionAmount = RandomUtils.round(amount.getAsDouble(), 2);

            if (subcommandGroup.equals("admin")) {
                switch (subcommand) {
                    case "add" -> {
                        EconomyUtil.pay(EconomyUtil.getMoneyHolder(commandEvent.getMember()),
                                EconomyUtil.getMoneyHolder(member), optionAmount, false,
                                transferToBank, true);
                        commandEvent.reply(commandEvent.getResource("message.money.add", EconomyUtil.formatMoney(optionAmount), member.getAsMention()), 5);
                    }
                    case "set" -> {
                        EconomyUtil.set(EconomyUtil.getMoneyHolder(member), optionAmount, transferToBank);
                        commandEvent.reply(commandEvent.getResource("message.money.set", member.getAsMention(), EconomyUtil.formatMoney(optionAmount)), 5);
                    }
                    case "remove" -> {
                        EconomyUtil.pay(EconomyUtil.getMoneyHolder(commandEvent.getMember()),
                                EconomyUtil.getMoneyHolder(member), -optionAmount, false,
                                transferToBank, true);
                        commandEvent.reply(commandEvent.getResource("message.money.remove", EconomyUtil.formatMoney(optionAmount), member.getAsMention()), 5);
                    }
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("money", "command.description.money.default")
                .addSubcommands(new SubcommandData("withdraw", "Withdraw your money from your bank into your pockets!")
                                .addOption(OptionType.NUMBER, "amount", "The amount of money you want to withdraw.", true),
                        new SubcommandData("deposit", "Deposit your money from your pockets into your bank!")
                                .addOption(OptionType.NUMBER, "amount", "The amount of money you want to deposit", true),
                        new SubcommandData("send", "Send money to another user!")
                                .addOption(OptionType.USER, "user", "The user you want to send money to.", true)
                                .addOption(OptionType.NUMBER, "amount", "The amount of money you want to pay.", true),
                        new SubcommandData("balance", "Check a users balance!")
                                .addOption(OptionType.USER, "user", "The user you want to check the balance of.", false))
                .addSubcommandGroups(new SubcommandGroupData("admin", "command.description.money.admin")
                        .addSubcommands(new SubcommandData("add", "Add money to a user!")
                                        .addOption(OptionType.USER, "user", "The user you want to add money to.", true)
                                        .addOptions(new OptionData(OptionType.NUMBER, "amount", "The amount of money you want to set.", true).setMinValue(0).setMaxValue(999999999))
                                        .addOption(OptionType.BOOLEAN, "bank", "If the money should be set in the bank.", false),
                                new SubcommandData("set", "Set the money of a user!")
                                        .addOption(OptionType.USER, "user", "The user you want to set the money of.", true)
                                        .addOptions(new OptionData(OptionType.NUMBER, "amount", "The amount of money you want to set.", true).setMinValue(0).setMaxValue(999999999))
                                        .addOption(OptionType.BOOLEAN, "bank", "If the money should be set in the bank.", false),
                                new SubcommandData("remove", "Remove money from a user!")
                                        .addOption(OptionType.USER, "user", "The user you want to remove money from.", true)
                                        .addOptions(new OptionData(OptionType.NUMBER, "amount", "The amount of money you want to set.", true).setMinValue(0).setMaxValue(999999999))
                                        .addOption(OptionType.BOOLEAN, "bank", "If the money should be set in the bank.", false)));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
