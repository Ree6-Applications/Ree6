package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.apache.commons.validator.GenericValidator;

/**
 * This command is used to let the bot remember your Birthday.
 */
@Command(name = "birthday", description = "command.description.birthday", category = Category.COMMUNITY)
public class Birthday implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        String command = commandEvent.getSubcommand();
        OptionMapping userMapping = commandEvent.getOption("user");
        OptionMapping dateMapping = commandEvent.getOption("birthday");

        switch (command) {
            case "remove" -> {
                if (userMapping == null) {
                    SQLSession.getSqlConnector().getSqlWorker().removeBirthday(commandEvent.getGuild().getIdLong(), commandEvent.getMember().getIdLong());
                    commandEvent.reply(commandEvent.getResource("message.birthday.removed.self"), 5);
                } else {
                    Member member = userMapping.getAsMember();
                    if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        if (member == null) {
                            commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                            return;
                        }

                        SQLSession.getSqlConnector().getSqlWorker().removeBirthday(commandEvent.getGuild().getIdLong(), member.getIdLong());
                        commandEvent.reply(commandEvent.getResource("message.birthday.removed.other", member.getAsMention()), 5);
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.birthday.removed.noPerms"), 5);
                    }
                }
            }

            default -> {
                String date = dateMapping.getAsString();
                if (userMapping == null) {
                    if (GenericValidator.isDate(date, "dd.MM.yyyy", true)) {
                        SQLSession.getSqlConnector().getSqlWorker().addBirthday(commandEvent.getGuild().getIdLong(), commandEvent.getChannel().getIdLong(), commandEvent.getMember().getIdLong(), date);
                        commandEvent.reply(commandEvent.getResource("message.birthday.added.self"), 5);
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.default.time.date"), 5);
                    }
                } else {
                    Member member = userMapping.getAsMember();
                    if (GenericValidator.isDate(date, "dd.MM.yyyy", true)) {
                        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            if (member == null) {
                                commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                            } else {
                                SQLSession.getSqlConnector().getSqlWorker().addBirthday(commandEvent.getGuild().getIdLong(), commandEvent.getChannel().getIdLong(), member.getIdLong(), date);
                                commandEvent.reply(commandEvent.getResource("message.birthday.added.other", member.getAsMention()), 5);
                            }
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.birthday.added.noPerms"), 5);
                        }
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.default.time.date"), 5);
                    }
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("birthday",
                LanguageService.getDefault("command.description.birthday"))
                .addSubcommands(new SubcommandData("remove", "Remove a Birthday entry!")
                                .addOptions(new OptionData(OptionType.USER, "user", "The User which should get their birthday entry removed.", false)),
                        new SubcommandData("add", "Add a Birthday entry!")
                                .addOptions(new OptionData(OptionType.STRING, "birthday", "The Birthday that should be added. Format -> day.month.year", true),
                                        new OptionData(OptionType.USER, "user", "The User which should get their birthday entry added.", false)));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"bday"};
    }
}
