package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.concurrent.TimeUnit;

/**
 * A command to ban a user from the server.
 */
@Command(name = "ban", description = "command.description.ban", category = Category.MOD)
public class Ban implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.BAN_MEMBERS.name()), 5);
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            if (commandEvent.isSlashCommand()) {
                OptionMapping targetOption = commandEvent.getOption("target");
                OptionMapping reasonOption = commandEvent.getOption("reason");
                OptionMapping deleteDaysOption = commandEvent.getOption("del_days");

                if (targetOption != null) {
                    if (reasonOption != null) {
                        banMember(targetOption.getAsMember(), reasonOption.getAsString(), (deleteDaysOption != null ? deleteDaysOption.getAsInt() : 0), commandEvent);
                    } else {
                        banMember(targetOption.getAsMember(), null, (deleteDaysOption != null ? deleteDaysOption.getAsInt() : 0), commandEvent);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                }
            } else {
                if (commandEvent.getArguments().length > 0) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","ban @user"), 5);
                    } else {
                        if (commandEvent.getArguments().length == 1) {
                            banMember(commandEvent.getMessage().getMentions().getMembers().get(0), null, 7, commandEvent);
                        } else {
                            StringBuilder reason = new StringBuilder();
                            for (int i = 1; i < commandEvent.getArguments().length; i++) {
                                reason.append(commandEvent.getArguments()[i]).append(" ");
                            }

                            banMember(commandEvent.getMessage().getMentions().getMembers().get(0), reason.toString(), 7, commandEvent);
                        }
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage","ban @user [reason]"), 5);
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.name()), 5);
        }

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("ban", LanguageService.getDefault("command.description.ban"))
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be banned.", true))
                .addOptions(new OptionData(OptionType.INTEGER, "del_days", "Delete messages from the past days.")
                        .setRequiredRange(0, 7))
                .addOptions(new OptionData(OptionType.STRING, "reason", "Why do you want to ban this User?"))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }

    /**
     * Ban a specific user from the server.
     *
     * @param member The user to ban.
     * @param reason The reason why the user should be banned.
     * @param deleteTime The deletion time in days.
     * @param commandEvent The command event.
     */
    public void banMember(Member member, String reason, int deleteTime, CommandEvent commandEvent) {
        if (member == null) {
            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
            return;
        }

        if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member)) {
            commandEvent.reply(commandEvent.getResource("message.ban.success", member.getUser().getName()), 5);
            commandEvent.getGuild().ban(member, deleteTime, TimeUnit.DAYS).reason(reason).queue();
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                commandEvent.reply(commandEvent.getResource("message.ban.hierarchySelfError"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.ban.hierarchyBotError"), 5);
            }
        }
    }
}
