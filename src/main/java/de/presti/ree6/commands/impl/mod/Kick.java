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
 * A command to kick a user from the server.
 */
@Command(name = "kick", description = "command.description.kick", category = Category.MOD)
public class Kick implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.KICK_MEMBERS.name()), 5);
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.KICK_MEMBERS)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getOption("target");
                OptionMapping reasonOption = commandEvent.getOption("reason");

                if (targetOption != null) {
                    kickMember(targetOption.getAsMember(), (reasonOption != null ? reasonOption.getAsString() : "No Reason given!"), commandEvent);
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                }

            } else {
                if (commandEvent.getArguments().length <= 1 && commandEvent.getArguments().length <= 2) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","kick @user"), 5);
                    } else {
                        String reason = commandEvent.getArguments().length == 2 ? commandEvent.getArguments()[1] : "No Reason given!";
                        kickMember(commandEvent.getMessage().getMentions().getMembers().get(0), reason, commandEvent);
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage","kick @user"), 5);
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.KICK_MEMBERS.name()), 5);
        }

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("kick", LanguageService.getDefault("command.description.kick"))
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be kicked.").setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "reason", "The Reason why the User should be kicked.").setRequired(false))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }

    /**
     * Kick a specific Member from the Server.
     * @param member The Member to kick.
     * @param reason The reason why the Member is being kicked.
     * @param commandEvent The CommandEvent.
     */
    public void kickMember(Member member, String reason, CommandEvent commandEvent) {
        if (member == null) {
            commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
            return;
        }

        if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member)) {
            commandEvent.reply(commandEvent.getResource("message.kick.success", member.getAsMention()), 5);
            commandEvent.getGuild().kick(member).reason(reason).queue();
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                commandEvent.reply(commandEvent.getResource("message.kick.hierarchySelfError"), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("message.kick.hierarchyBotError"), 5);
            }
        }
    }
}
