package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Punishments;
import de.presti.ree6.sql.entities.Warning;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A command to warn users and let them be punished if they reached a specific warn amount.
 */
@Command(name = "warn", description = "command.description.warn", category = Category.MOD)
public class Warn implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MODERATE_MEMBERS.name()), 5);
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MODERATE_MEMBERS.name()), 5);
            return;
        }

        String subCommandGroup = commandEvent.getSubcommandGroup();
        String subCommand = commandEvent.getSubcommand();
        OptionMapping userMapping = commandEvent.getOption("user");
        OptionMapping warningMapping = commandEvent.getOption("warnings");
        OptionMapping roleMapping = commandEvent.getOption("role");
        OptionMapping secondsMapping = commandEvent.getOption("seconds");
        OptionMapping reasonMapping = commandEvent.getOption("reason");
        OptionMapping idMapping = commandEvent.getOption("id");

        switch (subCommandGroup) {
            case "punishments" -> {
                if (!commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                    commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.name()), 5);
                    return;
                }

                switch (subCommand) {
                    case "roleadd" -> {
                        Punishments punishments = new Punishments();
                        punishments.setGuildId(commandEvent.getGuild().getIdLong());
                        punishments.setWarnings(warningMapping.getAsInt());
                        punishments.setAction(2);
                        punishments.setRoleId(roleMapping.getAsRole().getIdLong());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments);
                        commandEvent.reply(commandEvent.getResource("message.warn.punishment.created"));
                    }

                    case "roleremove" -> {
                        Punishments punishments = new Punishments();
                        punishments.setGuildId(commandEvent.getGuild().getIdLong());
                        punishments.setWarnings(warningMapping.getAsInt());
                        punishments.setAction(3);
                        punishments.setRoleId(roleMapping.getAsRole().getIdLong());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments);
                        commandEvent.reply(commandEvent.getResource("message.warn.punishment.created"));
                    }

                    case "timeout" -> {
                        Punishments punishments = new Punishments();
                        punishments.setGuildId(commandEvent.getGuild().getIdLong());
                        punishments.setWarnings(warningMapping.getAsInt());
                        punishments.setAction(1);
                        punishments.setTimeoutTime(secondsMapping.getAsLong() * 1000);
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments);
                        commandEvent.reply(commandEvent.getResource("message.warn.punishment.created"));
                    }

                    case "kick" -> {
                        Punishments punishments = new Punishments();
                        punishments.setGuildId(commandEvent.getGuild().getIdLong());
                        punishments.setWarnings(warningMapping.getAsInt());
                        punishments.setAction(4);
                        if (reasonMapping != null) punishments.setReason(reasonMapping.getAsString());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments);
                        commandEvent.reply(commandEvent.getResource("message.warn.punishment.created"));
                    }

                    case "ban" -> {
                        Punishments punishments = new Punishments();
                        punishments.setGuildId(commandEvent.getGuild().getIdLong());
                        punishments.setWarnings(warningMapping.getAsInt());
                        punishments.setAction(5);
                        if (reasonMapping != null) punishments.setReason(reasonMapping.getAsString());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments);
                        commandEvent.reply(commandEvent.getResource("message.warn.punishment.created"));
                    }

                    case "list" -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Punishments punishments : SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Punishments(), "FROM Punishments WHERE guildAndId.guildId = :gid", Map.of("gid", commandEvent.getGuild().getIdLong()))) {
                            int action = punishments.getAction();
                            stringBuilder.append(punishments.getId()).append(" - ").append(punishments.getWarnings()).append(" -> ");

                            switch (action) {
                                case 1 ->
                                        stringBuilder.append(commandEvent.getResource("message.warn.punishment.listEntry.timeout", Duration.ofMillis(punishments.getTimeoutTime()).toSeconds()));
                                case 2 ->
                                        stringBuilder.append(commandEvent.getResource("message.warn.punishment.listEntry.roleAdd", punishments.getRoleId()));
                                case 3 ->
                                        stringBuilder.append(commandEvent.getResource("message.warn.punishment.listEntry.roleRemove", punishments.getRoleId()));
                                case 4 ->
                                        stringBuilder.append(commandEvent.getResource("message.warn.punishment.listEntry.kick", punishments.getReason()));
                                case 5 ->
                                        stringBuilder.append(commandEvent.getResource("message.warn.punishment.listEntry.ban", punishments.getReason()));
                            }

                            stringBuilder.append("\n");
                        }
                        commandEvent.reply(commandEvent.getResource("message.warn.punishment.list", stringBuilder.toString()));
                    }

                    case "delete" -> {
                        int id = idMapping.getAsInt();
                        Punishments punishment = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Punishments(), "FROM Punishments WHERE guildAndId.guildId = :gid AND guildAndId.id = :id", Map.of("gid", commandEvent.getGuild().getIdLong(), "id", id));
                        if (punishment != null) {
                            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(punishment);
                            commandEvent.reply(commandEvent.getResource("message.warn.punishment.deleted", id));
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.warn.punishment.notFound", id));
                        }
                    }
                }
            }

            default -> {
                Member member = userMapping.getAsMember();
                if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member)) {
                    Warning warning = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Warning(), "FROM Warning WHERE guildUserId.guildId = :gid AND guildUserId.userId = :uid", Map.of("gid", commandEvent.getGuild().getIdLong(), "uid", member.getIdLong()));
                    int warnings = warning != null ? warning.getWarnings() + 1 : 1;
                    if (warning == null) {
                        warning = new Warning();
                        warning.setUserId(member.getIdLong());
                        warning.setGuildId(commandEvent.getGuild().getIdLong());
                    }

                    warning.setWarnings(warnings);

                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(warning);

                    commandEvent.reply(commandEvent.getResource("message.warn.success", userMapping.getAsMember().getAsMention(), warnings));
                    Punishments punishment = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Punishments(), "FROM Punishments WHERE guildAndId.guildId = :gid AND warnings = :amount", Map.of("gid", commandEvent.getGuild().getIdLong(), "amount", warnings));
                    if (punishment != null) {
                        switch (punishment.getAction()) {
                            case 1 -> member.timeoutFor(Duration.ofMillis(punishment.getTimeoutTime())).reason(commandEvent.getResource("message.warn.reachedWarnings", warnings)).queue();
                            case 2 -> {
                                Role role = commandEvent.getGuild().getRoleById(punishment.getRoleId());
                                if (role == null) {
                                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(punishment);
                                    return;
                                }

                                commandEvent.getGuild().addRoleToMember(member, role).reason(commandEvent.getResource("message.warn.reachedWarnings", warnings)).queue();
                            }
                            case 3 -> {
                                Role role = commandEvent.getGuild().getRoleById(punishment.getRoleId());
                                if (role == null) {
                                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(punishment);
                                    return;
                                }

                                commandEvent.getGuild().removeRoleFromMember(member, role).reason(commandEvent.getResource("message.warn.reachedWarnings", warnings)).queue();
                            }
                            case 4 -> member.kick().reason(punishment.getReason()).queue();
                            case 5 -> member.ban(0, TimeUnit.DAYS).reason(punishment.getReason()).queue();
                            default -> log.error("Unhandled action! {}", punishment.getAction());
                        }
                    }
                } else {
                    if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                        commandEvent.reply(commandEvent.getResource("message.warn.hierarchySelfError"), 5);
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.warn.hierarchyBotError"), 5);
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
        return new CommandDataImpl("warn", "command.description.warn")
                .addSubcommands(new SubcommandData("target", "Warn a specific player!")
                        .addOption(OptionType.USER, "user", "The user that should be warned.", true))
                .addSubcommandGroups(new SubcommandGroupData("punishments", "Set up the punishments for these warnings.")
                        .addSubcommands(new SubcommandData("timeout", "A Timeout punishment.")
                                .addOption(OptionType.INTEGER, "warnings", "The needed warning amounts.", true)
                                .addOption(OptionType.INTEGER, "seconds", "The seconds of how long the timeout should go on.", true))
                        .addSubcommands(new SubcommandData("roleadd", "A Punishment where you add a role to the user.")
                                .addOption(OptionType.INTEGER, "warnings", "The needed warning amounts.", true)
                                .addOption(OptionType.ROLE, "role", "The role the user should receive.", true))
                        .addSubcommands(new SubcommandData("roleremove", "A Punishment where you remove a role from the user.")
                                .addOption(OptionType.INTEGER, "warnings", "The needed warning amounts.", true)
                                .addOption(OptionType.ROLE, "role", "The role the user should get removed.", true))
                        .addSubcommands(new SubcommandData("kick", "A Kick punishment.")
                                .addOption(OptionType.INTEGER, "warnings", "The needed warning amounts.", true)
                                .addOption(OptionType.STRING, "reason", "A custom message", false))
                        .addSubcommands(new SubcommandData("ban", "A Ban punishment.")
                                .addOption(OptionType.INTEGER, "warnings", "The needed warning amounts.", true)
                                .addOption(OptionType.STRING, "reason", "A custom message.", false))
                        .addSubcommands(new SubcommandData("list", "A list of all punishments."))
                        .addSubcommands(new SubcommandData("delete", "Delete a specific punishment.")
                                .addOptions(new OptionData(OptionType.INTEGER, "id", "The Punishment Id.", true).setMinValue(0))));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
