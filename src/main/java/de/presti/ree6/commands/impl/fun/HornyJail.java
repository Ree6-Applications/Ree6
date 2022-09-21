package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.ImageCreationUtility;
import de.presti.ree6.utils.data.Language;
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
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null) {
                sendHornyJail(targetOption.getAsMember(), commandEvent);
            } else {
                Main.getInstance().getCommandManager().sendMessage("No User was given to put into the Hornyjail!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                    Main.getInstance().getCommandManager().sendMessage("No User given!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                } else {
                    sendHornyJail(commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent);
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "hornyjail @User", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("hornyjail", Language.getResource("en", "message.description.hornyjail")).addOptions(new OptionData(OptionType.USER, "target", "The User that should be put into the Hornyjail!").setRequired(true));
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
            Main.getInstance().getCommandManager().sendMessage(member.getAsMention() + " is now in the Hornyjail!", commandEvent.getChannel(), commandEvent.getInteractionHook());
            commandEvent.getChannel().sendMessage(createBuilder.build()).queue();
            if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
        } catch (Exception ex) {
            Main.getInstance().getCommandManager().sendMessage("Error while putting someone in the Hornyjail!\nError: " + ex.getMessage().replaceAll(Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken"), "Ree6TopSecretAPIToken"), commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
    }
}