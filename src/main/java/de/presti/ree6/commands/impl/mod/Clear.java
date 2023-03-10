package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A command to clear messages.
 */
@Command(name = "clear", description = "command.description.clear", category = Category.MOD)
public class Clear implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MESSAGE_MANAGE.name()), 5);
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            if (commandEvent.isSlashCommand()) {
                OptionMapping amountOption = commandEvent.getOption("amount");

                if (amountOption != null) {
                    deleteMessages(commandEvent, (int) amountOption.getAsDouble());
                }
            } else {
                if (commandEvent.getArguments().length == 1) {
                    try {
                        int amount = Integer.parseInt(commandEvent.getArguments()[0]);
                        if (amount <= 200 && amount >= 2) {
                            deleteMessages(commandEvent, amount);
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.clear.notInRange", commandEvent.getArguments()[0]), 5);
                            commandEvent.reply(commandEvent.getResource("message.default.usage", "clear 2-200"), 5);
                        }
                    } catch (Exception ex) {
                        commandEvent.reply(commandEvent.getResource("message.clear.noNumber",commandEvent.getArguments()[0]), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","clear 2-200"), 5);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage", "clear 2-200"), 5);
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MESSAGE_MANAGE.name()), 5);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("clear", LanguageService.getDefault("command.description.clear"))
                .addOptions(new OptionData(OptionType.INTEGER, "amount", "How many messages should be removed.")
                        .setRequired(true)
                        .setMinValue(2)
                        .setMaxValue(200))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"cc", "clearchat", "chatclear"};
    }

    /**
     * Delete Messages.
     * @param commandEvent CommandEvent.
     * @param amount Amount of Messages.
     */
    public void deleteMessages(CommandEvent commandEvent, int amount) {

        if (amount <= 200 && amount >= 2) {
            try {
                Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
                commandEvent.getChannel().getIterableHistory().takeAsync(amount).thenAccept(messages -> {
                    commandEvent.getChannel().purgeMessages(messages);
                    commandEvent.reply(commandEvent.getResource("message.clear.success", amount), 5);
                }).exceptionally(throwable -> {
                    commandEvent.reply(commandEvent.getResource("command.perform.errorWithException", throwable.getMessage()), 5);
                    return null;
                });
            } catch (Exception exception) {
                commandEvent.reply(commandEvent.getResource("command.perform.errorWithException", exception.getMessage()), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.clear.notInRange", commandEvent.getArguments()[0]), 5);
            commandEvent.reply(commandEvent.getResource("message.default.usage", "clear 2-200"), 5);
        }
    }
}
