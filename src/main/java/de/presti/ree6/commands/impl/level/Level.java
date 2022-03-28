package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.*;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.UserLevel;
import de.presti.ree6.utils.data.ImageCreationUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Command(name = "level", description = "Show your own Level or the Level of another User in the Guild.", category = Category.LEVEL)
public class Level implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendLevel(targetOption.getAsMember(), commandEvent);
            } else {
                sendLevel(commandEvent.getMember(), commandEvent);
            }
        } else {
            if (commandEvent.getArguments().length <= 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    sendLevel(commandEvent.getMember(), commandEvent);
                } else {
                    sendLevel(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level or " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level @user", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("level", "Show your own Level or the Level of another User in the Guild.")
                .addOptions(new OptionData(OptionType.USER, "target", "Show the Level of the User."));
    }

    @Override
    public String[] getAlias() {
        return new String[] {"lvl", "xp", "rank"};
    }

    public void sendLevel(Member member, CommandEvent commandEvent) {

        // TODO Add Voice Level support.

        UserLevel userLevel = Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getId(), member.getId());

        userLevel.setUser(member.getUser());
        if (commandEvent.isSlashCommand()) {
            try {
                commandEvent.getInteractionHook().sendFile(ImageCreationUtility.createRankImage(userLevel), "rank.png").queue();
            } catch (Exception ignore) {
                Main.getInstance().getCommandManager().sendMessage("Couldn't generated Rank Image!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            try {
                commandEvent.getTextChannel().sendFile(ImageCreationUtility.createRankImage(userLevel),"rank.png").queue();
            }  catch (Exception ignore) {
                Main.getInstance().getCommandManager().sendMessage("Couldn't generated Rank Image!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
