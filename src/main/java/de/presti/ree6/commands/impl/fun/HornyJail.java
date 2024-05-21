package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
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
 * A command to send someone to the horny-jail.
 */
@Command(name = "hornyjail", description = "command.description.hornyjail", category = Category.FUN)
public class HornyJail implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getOption("target");

            if (targetOption != null) {
                sendHornyJail(targetOption.getAsMember(), commandEvent);
            } else {
               commandEvent.reply(commandEvent.getResource("message.default.noMention.user"),5);
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                } else {
                    sendHornyJail(commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent);
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.usage","hornyjail @User"), 5);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("behave", "command.description.hornyjail_slash")
                .addOptions(new OptionData(OptionType.USER, "target", "The User that should be put into the Hornyjail!").setRequired(true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"horny", "jail"};
    }

    /**
     * Sends the given User into the horny-jail.
     * @param member The User that should be put into the horny-jail.
     * @param commandEvent The CommandEvent.
     */
    public void sendHornyJail(Member member, CommandEvent commandEvent) {
        try {
            MessageCreateBuilder createBuilder = new MessageCreateBuilder();
            createBuilder.addFiles(FileUpload.fromData(ImageCreationUtility.createHornyJailImage(member.getUser()), "hornyjail.png"));
            Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("message.hornyJail", member.getAsMention()), commandEvent.getChannel(), commandEvent.getInteractionHook());
            commandEvent.getChannel().sendMessage(createBuilder.build()).queue();
            if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.default.checkBelow")).queue();
        } catch (Exception ex) {
            Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.perform.error"), commandEvent.getChannel(), commandEvent.getInteractionHook());
            log.error("Error while sending Horny-jail!", ex);
        }
    }
}