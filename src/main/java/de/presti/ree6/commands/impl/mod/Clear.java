package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

@Command(name = "clear", description = "Clear the current Chat.", category = Category.MOD)
public class Clear implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            if (commandEvent.isSlashCommand()) {
                OptionMapping amountOption = commandEvent.getSlashCommandInteractionEvent().getOption("amount");

                if (amountOption != null) {
                    deleteMessages(commandEvent, (int) amountOption.getAsDouble());
                }
            } else {
                if (commandEvent.getArguments().length == 1) {
                    try {
                        int amount = Integer.parseInt(commandEvent.getArguments()[0]);
                        if (amount <= 100 && amount >= 2) {
                            deleteMessages(commandEvent, amount);
                        } else {
                            Main.getInstance().getCommandManager().sendMessage(commandEvent.getArguments()[0] + " isn't between 2 and 100 !", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        }
                    } catch (Exception ex) {
                        Main.getInstance().getCommandManager().sendMessage(commandEvent.getArguments()[0] + " isn't a number!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("clear", "Clear the Chat!").addOptions(new OptionData(OptionType.INTEGER, "amount", "How many messages should be removed.").setRequired(true));
    }

    @Override
    public String[] getAlias() {
        return new String[]{"cc", "clearchat", "chatclear"};
    }

    public void deleteMessages(CommandEvent commandEvent, int amount) {

        if (amount <= 100 && amount >= 2) {
            try {
                Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
                List<Message> messages = commandEvent.getTextChannel().getHistory().retrievePast(amount).complete();
                commandEvent.getTextChannel().deleteMessages(messages).queue();

                Main.getInstance().getCommandManager().sendMessage(messages.size() + " Messages have been deleted!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } catch (IllegalArgumentException exception) {
                Main.getInstance().getCommandManager().sendMessage("" + (exception.toString().toLowerCase().startsWith("java.lang.illegalargumentexception: must provide at") ? "Given Parameter is either above 100 or below 2!" : "Error while deleting:" + exception.toString().split(":")[1]), 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } catch (Exception exception) {
                Main.getInstance().getCommandManager().sendMessage("Error while deleting:" + exception.toString().split(":")[1], 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage(amount + " isn't between 2 and 100 !", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}
