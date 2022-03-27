package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.UserLevel;
import de.presti.ree6.utils.data.ImageCreationUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Level extends Command {

    public Level() {
        super("level", "Shows the Level of a User!", Category.LEVEL, new String[] {"lvl", "xp", "rank"}, new CommandDataImpl("level", "Shows the Level of a User!")
                .addOptions(new OptionData(OptionType.USER, "target", "Show the Level of the User.")));
    }

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
                sendMessage("Not enough Arguments!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level or " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level @user", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    public void sendLevel(Member member, CommandEvent commandEvent) {

        // Add Voice Level support.

        UserLevel userLevel = Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getId(), member.getId());

        userLevel.setUser(member.getUser());
        if (commandEvent.isSlashCommand()) {
            try {
                commandEvent.getInteractionHook().sendFile(ImageCreationUtility.createRankImage(userLevel), "rank.png").queue();
            } catch (Exception ignore) {
                sendMessage("Couldn't generated Rank Image!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            try {
                commandEvent.getTextChannel().sendFile(ImageCreationUtility.createRankImage(userLevel),"rank.png").queue();
            }  catch (Exception ignore) {
                sendMessage("Couldn't generated Rank Image!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
