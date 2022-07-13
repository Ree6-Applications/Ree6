package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
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
@Command(name = "unmute", description = "Unmute a specific user on the Server.", category = Category.MOD)
public class Unmute implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            Main.getInstance().getCommandManager().sendMessage("It seems like I do not have the permissions to do that :/\nPlease re-invite me!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

                if (targetOption != null) {
                    unmuteMember(commandEvent.getMember(), targetOption.getAsMember(), commandEvent);
                } else {
                    Main.getInstance().getCommandManager().sendMessage("No User was given to Unmute!" , 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }

            } else {
                if (commandEvent.getArguments().length == 1) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        Main.getInstance().getCommandManager().sendMessage("No User mentioned!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "unmute @user", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else {
                        unmuteMember(commandEvent.getMember(), commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent);
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "unmute @user", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("unmute", "Unmute a User on the Server!")
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
     * Unmutes a Member.
     * @param executor The Executor.
     * @param member The Member to unmute.
     * @param commandEvent The CommandEvent.
     */
    public void unmuteMember(Member executor, Member member, CommandEvent commandEvent) {

        if (executor.canInteract(member) && commandEvent.getGuild().getSelfMember().canInteract(member)) {
            member.removeTimeout().onErrorFlatMap(throwable -> {
                Main.getInstance().getCommandManager().sendMessage("Could not unmute " + member.getUser().getAsTag() + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                return null;
            }).queue(unused -> {
                Main.getInstance().getCommandManager().sendMessage("User " + member.getAsMention() + " was unmuted!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            });
            Main.getInstance().getCommandManager().sendMessage("User " + member.getAsMention() + " has been unmuted!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        } else {
            if (!executor.canInteract(member)) {
                Main.getInstance().getCommandManager().sendMessage("You cannot unmute " + member.getUser().getAsTag() + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("I cannot unmute " + member.getUser().getAsTag() + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
