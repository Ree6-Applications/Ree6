package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.level.UserLevel;
import de.presti.ree6.utils.data.ImageCreationUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A command to see your current Level.
 */
@Command(name = "level", description = "command.description.level", category = Category.LEVEL)
public class
Level implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getOption("target");
            OptionMapping levelType = commandEvent.getOption("typ");

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
                commandEvent.reply(commandEvent.getResource("message.default.usage","level chat/voice [@user]"));
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("level", LanguageService.getDefault("command.description.level")).addOptions(new OptionData(OptionType.STRING, "typ", "Do you want to see chat or voice level?"))
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
                SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(commandEvent.getGuild().getIdLong(), member.getIdLong()) :
                SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getIdLong(), member.getIdLong());

            try {
                MessageCreateBuilder createBuilder = new MessageCreateBuilder();
                createBuilder.addFiles(FileUpload.fromData(ImageCreationUtility.createRankImage(userLevel), "rank.png"));

                commandEvent.reply(createBuilder.build());
            } catch (Exception exception) {
                commandEvent.reply(commandEvent.getResource("command.perform.error"));
                log.error("Couldn't generated Rank Image!", exception);
            }
    }
}
