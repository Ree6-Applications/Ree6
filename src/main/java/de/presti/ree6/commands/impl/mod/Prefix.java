package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A command to change the prefix.
 */
@Command(name = "prefix", description = "command.description.prefix", category = Category.MOD)
public class Prefix implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR) && commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {

            if (commandEvent.isSlashCommand()) {
                OptionMapping prefixOption = commandEvent.getSlashCommandInteractionEvent().getOption("new-prefix");

                if (prefixOption != null) {
                    Main.getInstance().getSqlConnector().getSqlWorker().setSetting(commandEvent.getGuild().getId(), "chatprefix", prefixOption.getAsString());
                    Main.getInstance().getCommandManager().sendMessage("Your new Prefix has been set to: " + prefixOption.getAsString(), 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "prefix PREFIX", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            } else {
                if (commandEvent.getArguments().length != 1) {
                    Main.getInstance().getCommandManager().sendMessage((commandEvent.getArguments().length < 1 ? "Not enough" : "Too many") + " Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "prefix PREFIX", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                } else {
                    Main.getInstance().getSqlConnector().getSqlWorker().setSetting(commandEvent.getGuild().getId(), "chatprefix", commandEvent.getArguments()[0]);
                    Main.getInstance().getCommandManager().sendMessage("Your new Prefix has been set to: " + commandEvent.getArguments()[0], 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("prefix", "Change Ree6's Bot-Prefix!")
                .addOptions(new OptionData(OptionType.STRING, "new-prefix", "What should the new Prefix be?").setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR, Permission.MANAGE_SERVER));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"setprefix", "changeprefix"};
    }
}
