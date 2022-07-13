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
 * A command to ban a user from the server.
 */
@Command(name = "ban", description = "Ban an specific user from the Server.", category = Category.MOD)
public class Ban implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            Main.getInstance().getCommandManager().sendMessage("It seems like I do not have the permissions to do that :/\nPlease re-invite me!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            if (commandEvent.isSlashCommand()) {
                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");
                OptionMapping reasonOption = commandEvent.getSlashCommandInteractionEvent().getOption("reason");

                if (targetOption != null) {
                    if (reasonOption != null) {
                        banMember(targetOption.getAsMember(), reasonOption.getAsString(), commandEvent);
                    } else {
                        banMember(targetOption.getAsMember(), null, commandEvent);
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("No User was given to Ban!" , 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            } else {
                if (commandEvent.getArguments().length > 0) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        Main.getInstance().getCommandManager().sendMessage("No User mentioned!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "ban @user", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else {
                        if (commandEvent.getArguments().length == 1) {
                            banMember(commandEvent.getMessage().getMentions().getMembers().get(0), null, commandEvent);
                        } else {
                            StringBuilder reason = new StringBuilder();
                            for (int i = 1; i < commandEvent.getArguments().length; i++) {
                                reason.append(commandEvent.getArguments()[i]).append(" ");
                            }

                            banMember(commandEvent.getMessage().getMentions().getMembers().get(0), reason.toString(), commandEvent);
                        }
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "ban @user [reason]", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("ban", "Ban the User from the Server!")
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be banned.").setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, "del_days", "Delete messages from the past days.")
                        .setRequiredRange(0, 7))
                .addOptions(new OptionData(OptionType.STRING, "reason", "Why do you want to ban this User?").setRequired(false))
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
     * @param commandEvent The command event.
     */
    public void banMember(Member member, String reason, CommandEvent commandEvent) {
        if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member)) {
            Main.getInstance().getCommandManager().sendMessage("User " + member.getAsMention() + " has been banned!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            commandEvent.getGuild().ban(member, 7, reason).queue();
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                Main.getInstance().getCommandManager().sendMessage("Couldn't ban this User because he has the same or a higher Rank then you!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("Couldn't ban this User because he has a higher Rank then me!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
