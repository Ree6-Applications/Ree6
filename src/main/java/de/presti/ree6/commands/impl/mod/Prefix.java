package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
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
                OptionMapping prefixOption = commandEvent.getOption("new-prefix");

                if (prefixOption != null) {
                    SQLSession.getSqlConnector().getSqlWorker().setSetting(commandEvent.getGuild().getIdLong(), "chatprefix", "Prefix", prefixOption.getAsString());
                    commandEvent.reply(commandEvent.getResource("message.prefix.success", prefixOption.getAsString()), 5);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.usage","prefix PREFIX", 5));
                }
            } else {
                if (commandEvent.getArguments().length != 1) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage","prefix PREFIX", 5));
                } else {
                    SQLSession.getSqlConnector().getSqlWorker().setSetting(commandEvent.getGuild().getIdLong(), "chatprefix", "Prefix", commandEvent.getArguments()[0]);
                    commandEvent.reply(commandEvent.getResource("message.prefix.success", commandEvent.getArguments()[0]), 5);
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.name() + "/" + Permission.MANAGE_SERVER.name()));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("prefix", LanguageService.getDefault("command.description.prefix"))
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
