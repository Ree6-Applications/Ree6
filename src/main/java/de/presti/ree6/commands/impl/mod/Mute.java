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

import java.time.Duration;

@Command(name = "mute", description = "Mute a specific user on the Server.", category = Category.MOD)
public class Mute implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            Main.getInstance().getCommandManager().sendMessage("It seems like I do not have the permissions to do that :/\nPlease re-invite me!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {

            if (commandEvent.isSlashCommand()) {

                OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");
                OptionMapping timeOption = commandEvent.getSlashCommandInteractionEvent().getOption("time");
                OptionMapping reasonOption = commandEvent.getSlashCommandInteractionEvent().getOption("reason");

                if (targetOption != null && timeOption != null) {
                    long time;
                    try {
                        time = timeOption.getAsLong();
                    } catch (Exception ignore) {
                        Main.getInstance().getCommandManager().sendMessage("The given Time is not a valid Number!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        return;
                    }
                    Duration duration = Duration.ofMinutes(time);
                    muteMember(commandEvent.getMember(), targetOption.getAsMember(), duration, (reasonOption != null ? reasonOption.getAsString() : "No Reason given!"), commandEvent);
                } else {
                    Main.getInstance().getCommandManager().sendMessage("No User was given to Mute!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }

            } else {
                if (commandEvent.getArguments().length <= 2 && commandEvent.getArguments().length <= 3) {
                    if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                        Main.getInstance().getCommandManager().sendMessage("No User mentioned!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "mute @user", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else {
                        long time;
                        try {
                            time = Long.parseLong(commandEvent.getArguments()[1]);
                        } catch (Exception ignore) {
                            Main.getInstance().getCommandManager().sendMessage("The given Time is not a valid Number!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                            return;
                        }
                        Duration duration = Duration.ofMinutes(time);
                        String reason = commandEvent.getArguments().length == 3 ? commandEvent.getArguments()[2] : "No Reason given!";
                        muteMember(commandEvent.getMember(), commandEvent.getMessage().getMentions().getMembers().get(0), duration, reason, commandEvent);
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "mute @user", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("mute", "Mute a User on the Server!")
                .addOptions(new OptionData(OptionType.USER, "target", "Which User should be muted.").setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, "time", "How long the User should be muted for. (in minutes)").setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "reason", "The Reason why the User should be muted.").setRequired(false))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    public void muteMember(Member executor, Member member, Duration duration, String reason, CommandEvent commandEvent) {

        if (executor.canInteract(member) && commandEvent.getGuild().getSelfMember().canInteract(member)) {
            member.timeoutFor(duration).reason(reason).onErrorFlatMap(throwable -> {
                Main.getInstance().getCommandManager().sendMessage("Couldn't mute " + member.getAsMention() + "!\nReason: " + throwable.getMessage(), commandEvent.getChannel()
                        , commandEvent.getInteractionHook());
                return null;
            }).queue(unused -> Main.getInstance().getCommandManager().sendMessage(member.getAsMention() + " was muted for " + duration.getSeconds() + " seconds!", commandEvent.getChannel()
                    , commandEvent.getInteractionHook()));
        } else {
            if (!executor.canInteract(member)) {
                Main.getInstance().getCommandManager().sendMessage("I couldn't timeout the Member, because you do not have enough permissions!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getCommandManager().sendMessage("I couldn't timeout the Member, because I do not have enough permissions for this!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
