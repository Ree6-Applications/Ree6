package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Unban extends Command {

    public Unban() {
        super("unban", "Unban a User from the Server!", Category.MOD, new CommandDataImpl("unban", "Unban a User from the Server!").addOptions(new OptionData(OptionType.STRING, "id", "Which User should be unbanned.").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("id");

                if (targetOption != null) {
                    commandEvent.getGuild().unban(targetOption.getAsString()).queue();
                    sendMessage("User <@" + targetOption.getAsString() + "> has been unbanned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendMessage("No User was given to Unban!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }

            } else {
                if (commandEvent.getArguments().length == 1) {
                    String userId = commandEvent.getArguments()[0];
                    commandEvent.getGuild().unban(userId).queue();
                    sendMessage("User <@" + userId + "> has been unbanned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "unban @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}