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

public class Prefix extends Command {


    public Prefix() {
        super("prefix", "Change Ree6's Bot-Prefix!", Category.MOD, new String[]{"setprefix", "changeprefix"}, new CommandDataImpl("prefix", "Change Ree6's Bot-Prefix!").addOptions(new OptionData(OptionType.STRING, "new-prefix", "What should the new Prefix be?").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR) && commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {

            if (commandEvent.isSlashCommand()) {
                OptionMapping prefixOption = commandEvent.getSlashCommandInteractionEvent().getOption("new-prefix");

                if (prefixOption != null) {
                    Main.getInstance().getSqlConnector().getSqlWorker().setSetting(commandEvent.getGuild().getId(), "chatprefix", prefixOption.getAsString());
                    sendMessage("Your new Prefix has been set to: " + prefixOption.getAsString(), 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "prefix PREFIX", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            } else {
                if (commandEvent.getArguments().length != 1) {
                    sendMessage((commandEvent.getArguments().length < 1 ? "Not enough" : "Too many") + " Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "prefix PREFIX", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    Main.getInstance().getSqlConnector().getSqlWorker().setSetting(commandEvent.getGuild().getId(), "chatprefix", commandEvent.getArguments()[0]);
                    sendMessage("Your new Prefix has been set to: " + commandEvent.getArguments()[0], 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}
