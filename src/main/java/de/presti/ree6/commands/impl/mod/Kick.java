package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Kick extends CommandClass {

    public Kick() {
        super("kick", "Kick the User from the Server!", Category.MOD,  new CommandDataImpl("kick", "Kick the User from the Server!").addOptions(new OptionData(OptionType.USER, "target", "Which User should be kicked.").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

                if (targetOption != null) {
                    kickMember(targetOption.getAsMember(), commandEvent);
                } else {
                    sendMessage("No User was given to Kick!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }

            } else {
                if (commandEvent.getArguments().length == 1) {
                    if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                        sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "kick @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        kickMember(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                    }
                } else {
                    sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "kick @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    public void kickMember(Member member, CommandEvent commandEvent) {
        if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member) ) {
            sendMessage("User " + member.getAsMention() + " has been kicked!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            commandEvent.getGuild().kick(member).queue();
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                sendMessage("Couldn't kick this User because he has the same or a higher Rank then you!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } else {
                sendMessage("Couldn't kick this User because he has a higher Rank then me!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
