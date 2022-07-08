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

@Command(name = "kick", description = "Kick a specific user from the Server.", category = Category.MOD)
public class Kick implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
            Main.getInstance().getCommandManager().sendMessage("It seems like I do not have the permissions to do that :/\nPlease re-invite me!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.KICK_MEMBERS)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");
                OptionMapping reasonOption = commandEvent.getSlashCommandInteractionEvent().getOption("reason");

                if (targetOption != null) {
                    kickMember(targetOption.getAsMember(), (reasonOption != null ? reasonOption.getAsString() : "No Reason given!"), commandEvent);
                } else {
                    Main.getInstance().getCommandManager().sendMessage("No User was given to Kick!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }

            } else {
                if (commandEvent.getArguments().length <= 1 && commandEvent.getArguments().length <= 2) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        Main.getInstance().getCommandManager().sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "kick @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        String reason = commandEvent.getArguments().length == 2 ? commandEvent.getArguments()[1] : "No Reason given!";
                        kickMember(commandEvent.getMessage().getMentions().getMembers().get(0), reason, commandEvent);
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "kick @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("kick", "Kick the User from the Server!")
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be kicked.").setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "reason", "The Reason why the User should be kicked.").setRequired(false))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    public void kickMember(Member member, String reason, CommandEvent commandEvent) {
        if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member)) {
            Main.getInstance().getCommandManager().sendMessage("User " + member.getAsMention() + " has been kicked!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            commandEvent.getGuild().kick(member).reason(reason).queue();
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                Main.getInstance().getCommandManager().sendMessage("Couldn't kick this User because he has the same or a higher Rank then you!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("Couldn't kick this User because he has a higher Rank then me!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
