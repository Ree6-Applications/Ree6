package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Command(name = "mute", description = "Mute a specific user on the Server.", category = Category.MOD)
public class Mute implements ICommand {

    // TODO rework, to instead use the Timeout feature.

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if(!Main.getInstance().getSqlConnector().getSqlWorker().isMuteSetup(commandEvent.getGuild().getId())) {
                Main.getInstance().getCommandManager().sendMessage("Mute Role hasn't been set!\nTo set it up type " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup mute @MuteRole !", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                return;
            }

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

                if (targetOption != null) {
                    muteMember(targetOption.getAsMember(), commandEvent);
                } else {
                    Main.getInstance().getCommandManager().sendMessage("No User was given to Mute!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }

            } else {
                if (commandEvent.getArguments().length == 1) {
                    if(commandEvent.getMessage().getMentionedUsers().isEmpty()) {
                        Main.getInstance().getCommandManager().sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "mute @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        muteMember(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "mute @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("mute", "Mute a User on the Server!").addOptions(new OptionData(OptionType.USER, "target", "Which User should be muted.").setRequired(true));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    public void muteMember(Member member, CommandEvent commandEvent) {
        Role role = commandEvent.getGuild().getRoleById(Main.getInstance().getSqlConnector().getSqlWorker().getMuteRole(commandEvent.getGuild().getId()));

        if (role != null && commandEvent.getGuild().getSelfMember().canInteract(role) && commandEvent.getGuild().getSelfMember().canInteract(member)) {
            commandEvent.getGuild().addRoleToMember(member, role).queue();
            Main.getInstance().getCommandManager().sendMessage("User " + member.getAsMention() + " has been muted!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else {
            if (role == null) {
                Main.getInstance().getCommandManager().sendMessage("The Mute Role that has been set is invalid.", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("I can't interact with the wanted " + (commandEvent.getGuild().getSelfMember().canInteract(role) ? "Member" : "Muterole") + " !", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
