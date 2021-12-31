package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.LoggerImpl;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Clear extends Command {

    public Clear() {
        super("clear", "Clear the Chat!", Category.MOD, new CommandData("clear", "Clear the Chat!").addOptions(new OptionData(OptionType.INTEGER, "amount", "How many messages should be removed.").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if(sender.hasPermission(Permission.ADMINISTRATOR)) {
            if(args.length == 1) {
                try {
                    if (Integer.parseInt(args[0]) <= 100 && Integer.parseInt(args[0]) >= 2) {
                        try {
                            messageSelf.delete().queue();
                            List<Message> messages = m.getHistory().retrievePast(Integer.parseInt(args[0])).complete();
                            m.deleteMessages(messages).queue();

                            sendMessage(messages.size() + " Messages have been deleted!", 5, m, hook);

                        } catch (Exception ex) {
                            if(ex instanceof IllegalArgumentException) {
                                sendMessage("" + (ex.toString().toLowerCase().startsWith("java.lang.illegalargumentexception: must provide at") ? "Given Paramater is below 100 and 2!" : "Error while deleting:" + ex.toString().split(":")[1]), 5, m, hook);
                            } else {
                                sendMessage("Error while deleting:" + ex.toString().split(":")[1], 5, m, hook);
                            }
                        }

                    } else {
                        sendMessage(args[0] + " isn't between 2 and 100 !", 5, m, hook);
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "clear 1-100", 5, m, hook);
                    }
                } catch (Exception ex) {
                    sendMessage(args[0] + " isn't a number!", 5, m, hook);
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, m, hook);
                    LoggerImpl.log("Clear", ex.getMessage() + " - " + ex.getStackTrace()[0].toString());
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m, hook);
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "clear 2-100", 5, m, hook);
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, m, hook);
        }
    }
}
