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

import java.time.Duration;

/**
 * A command to mute a user.
 */
@Command(name = "mute", description = "command.description.mute", category = Category.MOD)
public class Mute implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MODERATE_MEMBERS.name()), 5);
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getOption("target");
                OptionMapping timeOption = commandEvent.getOption("time");
                OptionMapping reasonOption = commandEvent.getOption("reason");

                if (targetOption != null && timeOption != null && targetOption.getAsMember() != null) {
                    long time;
                    try {
                        time = timeOption.getAsLong();
                    } catch (Exception ignore) {
                        commandEvent.reply("message.command.mute.invalidTime", 5);
                        return;
                    }
                    Duration duration = Duration.ofMinutes(time);
                    muteMember(commandEvent.getMember(), targetOption.getAsMember(), duration, (reasonOption != null ? reasonOption.getAsString() : "No Reason given!"), commandEvent);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                }

            } else {
                if (commandEvent.getArguments().length <= 2 && commandEvent.getArguments().length <= 3) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","mute @user"), 5);
                    } else {
                        long time;
                        try {
                            time = Long.parseLong(commandEvent.getArguments()[1]);
                        } catch (Exception ignore) {
                            commandEvent.reply("message.command.mute.invalidTime", 5);
                            return;
                        }
                        Duration duration = Duration.ofMinutes(time);
                        String reason = commandEvent.getArguments().length == 3 ? commandEvent.getArguments()[2] : "No Reason given!";
                        muteMember(commandEvent.getMember(), commandEvent.getMessage().getMentions().getMembers().get(0), duration, reason, commandEvent);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage","mute @user"), 5);
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MODERATE_MEMBERS.name()), 5);
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("mute", LanguageService.getDefault("command.description.mute"))
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be muted.").setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, "time", "How long the User should be muted for. (in minutes)").setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "reason", "The Reason why the User should be muted.").setRequired(false))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }

    /**
     * Mutes a Member.
     * @param executor The Executor.
     * @param member The Member to mute.
     * @param duration The duration of the mute.
     * @param reason The reason of the mute.
     * @param commandEvent The CommandEvent.
     */
    public void muteMember(Member executor, Member member, Duration duration, String reason, CommandEvent commandEvent) {
        if (duration.toDays() > 28) {
            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
            return;
        }

        if (executor.canInteract(member) && commandEvent.getGuild().getSelfMember().canInteract(member)) {
            member.timeoutFor(duration).reason(reason).onErrorFlatMap(throwable -> {
                commandEvent.reply(commandEvent.getResource("command.perform.errorWithException", throwable.getMessage()));
                return null;
            }).queue(unused -> commandEvent.reply(commandEvent.getResource("message.mute.success",member.getAsMention(), duration.getSeconds())));
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                commandEvent.reply(commandEvent.getResource("message.mute.hierarchySelfError"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.mute.hierarchyBotError"), 5);
            }
        }
    }
}
