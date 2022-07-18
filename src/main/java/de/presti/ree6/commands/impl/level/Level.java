package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.level.UserLevel;
import de.presti.ree6.utils.data.ImageCreationUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A command to see your current Level.
 */
@Command(name = "level", description = "Show your own Level or the Level of another User in the Guild.", category = Category.LEVEL)
public class Level implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");
            OptionMapping levelType = commandEvent.getSlashCommandInteractionEvent().getOption("typ");

            if (targetOption != null && targetOption.getAsMember() != null && levelType != null) {
                sendLevel(targetOption.getAsMember(), commandEvent, levelType.getAsString());
            } else {
                sendLevel(commandEvent.getMember(), commandEvent, "chat");
            }
        } else {
            if (commandEvent.getArguments().length <= 2) {
                String typ = commandEvent.getArguments().length == 0 ? "chat"
                        : commandEvent.getArguments()[0];
                if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                    sendLevel(commandEvent.getMember(), commandEvent, typ);
                } else {
                    sendLevel(commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent, typ);
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", commandEvent.getChannel(), commandEvent.getInteractionHook());
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level chat/voice or " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level chat/voice @user", commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("level", "Show your own Level or the Level of another User in the Guild.").addOptions(new OptionData(OptionType.STRING, "typ", "Do you want to see chat or voice level?"))
                .addOptions(new OptionData(OptionType.USER, "target", "Show the Level of the User."));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] {"lvl", "xp", "rank"};
    }

    /**
     * Sends the Level of the User.
     * @param member The Member to get the Level of.
     * @param commandEvent The CommandEvent.
     * @param type The Type of the Level.
     */
    public void sendLevel(Member member, CommandEvent commandEvent, String type) {

        UserLevel userLevel = type.equalsIgnoreCase("voice") ?
                Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelData(commandEvent.getGuild().getId(), member.getId()) :
                Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getId(), member.getId());

        userLevel.setUser(commandEvent.getMember().getUser());

        if (commandEvent.isSlashCommand()) {
            try {
                commandEvent.getInteractionHook().sendFile(ImageCreationUtility.createRankImage(userLevel), "rank.png").queue();
            } catch (Exception ignore) {
                Main.getInstance().getCommandManager().sendMessage("Couldn't generated Rank Image!", commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else {
            try {
                commandEvent.getChannel().sendFile(ImageCreationUtility.createRankImage(userLevel), "rank.png").queue();
            } catch (Exception ignore) {
                Main.getInstance().getCommandManager().sendMessage("Couldn't generated Rank Image!", commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }
}
