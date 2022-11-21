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

/**
 * A command to unmute a user.
 */
@Command(name = "unmute", description = "command.description.unmute", category = Category.MOD)
public class Unmute implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MODERATE_MEMBERS.name()), 5);
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

                if (targetOption != null) {
                    unmuteMember(commandEvent.getMember(), targetOption.getAsMember(), commandEvent);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                }

            } else {
                if (commandEvent.getArguments().length == 1) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","unmute @user"), 5);
                    } else {
                        unmuteMember(commandEvent.getMember(), commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage","unmute @user"), 5);
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
        return new CommandDataImpl("unmute", LanguageService.getDefault("command.description.unmute"))
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be unmuted.").setRequired(true))
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
     * Unmute a Member.
     * @param executor The Executor.
     * @param member The Member to unmute.
     * @param commandEvent The CommandEvent.
     */
    public void unmuteMember(Member executor, Member member, CommandEvent commandEvent) {

        if (executor.canInteract(member) && commandEvent.getGuild().getSelfMember().canInteract(member)) {
            member.removeTimeout().onErrorFlatMap(throwable -> {
                commandEvent.reply(commandEvent.getResource("command.perform.errorWithException", throwable.getMessage()));
                return null;
            }).queue(unused ->commandEvent.reply(commandEvent.getResource("message.unmute.success",member.getAsMention())));
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                commandEvent.reply(commandEvent.getResource("message.unmute.hierarchySelfError"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.unmute.hierarchyBotError"), 5);
            }
        }
    }
}
