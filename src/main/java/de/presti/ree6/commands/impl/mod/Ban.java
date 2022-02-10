package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Ban extends Command {

    public Ban() {
        super("ban", "Ban the User from the Server!", Category.MOD, new CommandDataImpl("ban", "Ban the User from the Server!")
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be banned.").setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "reason", "Why do you want to ban this User?").setRequired(false)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
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
                    sendMessage("No User was given to Ban!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            } else {
                if (commandEvent.getArguments().length > 0) {
                    if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                        sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "ban @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        if (commandEvent.getArguments().length == 1) {
                            banMember(commandEvent.getMessage().getMentionedMembers().get(0), null, commandEvent);
                        } else {
                            StringBuilder reason = new StringBuilder();
                            for (int i = 1; i < commandEvent.getArguments().length; i++) {
                                reason.append(commandEvent.getArguments()[i]).append(" ");
                            }

                            banMember(commandEvent.getMessage().getMentionedMembers().get(0), reason.toString(), commandEvent);
                        }
                    }
                } else {
                    sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "ban @user [reason]", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    public void banMember(Member member, String reason, CommandEvent commandEvent) {
        if (commandEvent.getGuild().getSelfMember().canInteract(member) && commandEvent.getMember().canInteract(member)) {
            sendMessage("User " + member.getAsMention() + " has been banned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            commandEvent.getGuild().ban(member, 7, reason).queue();
        } else {
            if (commandEvent.getGuild().getSelfMember().canInteract(member)) {
                sendMessage("Couldn't ban this User because he has the same or a higher Rank then you!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } else {
                sendMessage("Couldn't ban this User because he has a higher Rank then me!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
