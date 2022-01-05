package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Clear extends Command {

    public Clear() {
        super("clear", "Clear the Chat!", Category.MOD, new CommandData("clear", "Clear the Chat!").addOptions(new OptionData(OptionType.INTEGER, "amount", "How many messages should be removed.").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if(commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping amountOption = commandEvent.getSlashCommandEvent().getOption("amount");

                if (amountOption != null) {
                    deleteMessages(commandEvent, (int) amountOption.getAsDouble());
                }

            } else {
                if (commandEvent.getArguments().length == 1) {
                    try {
                        int amount = Integer.parseInt(commandEvent.getArguments()[0]);
                        if (amount <= 100 && amount>= 2) {

                            deleteMessages(commandEvent, amount);

                        } else {
                            sendMessage(commandEvent.getArguments()[0] + " isn't between 2 and 100 !", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        }
                    } catch (Exception ex) {
                        sendMessage(commandEvent.getArguments()[0] + " isn't a number!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else {
                    sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    public void deleteMessages(CommandEvent commandEvent, int amount) {

        if (amount <= 100 && amount>= 2) {

            try {
                deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
                List<Message> messages = commandEvent.getTextChannel().getHistory().retrievePast(amount).complete();
                commandEvent.getTextChannel().deleteMessages(messages).queue();

                sendMessage(messages.size() + " Messages have been deleted!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

            } catch (Exception ex) {
                if (ex instanceof IllegalArgumentException) {
                    sendMessage("" + (ex.toString().toLowerCase().startsWith("java.lang.illegalargumentexception: must provide at") ? "Given Parameter is either above 100 or below 2!" : "Error while deleting:" + ex.toString().split(":")[1]), 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendMessage("Error while deleting:" + ex.toString().split(":")[1], 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }

        } else {
            sendMessage(amount + " isn't between 2 and 100 !", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}
