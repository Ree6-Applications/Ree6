package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.apis.Neko4JsAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

/**
 * A command to send someone a kiss.
 */
@Command(name = "kiss", description = "command.description.kiss", category = Category.FUN)
public class Kiss implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendKiss(targetOption.getAsMember(), commandEvent);
            } else {
                commandEvent.reply(commandEvent.getResource("command.message.default.noMention.user"), 5);
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                    commandEvent.reply(commandEvent.getResource("command.message.default.noMention.user"), 5);
                    commandEvent.reply(commandEvent.getResource("command.message.default.usage","kiss @user"), 5);
                } else {
                    sendKiss(commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent);
                }
            } else {
                commandEvent.reply(commandEvent.getResource("command.message.default.usage","kiss @user"), 5);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("kiss", LanguageService.getDefault("message.description.kiss"))
                .addOptions(new OptionData(OptionType.USER, "target", "The User that should be kissed!").setRequired(true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }

    /**
     * Sends a kiss to the given user.
     * @param member The user that should be kissed.
     * @param commandEvent The command event.
     */
    public void sendKiss(Member member, CommandEvent commandEvent) {
        Main.getInstance().getCommandManager().sendMessage(commandEvent.getResource("command.message.kiss", member.getAsMention(), commandEvent.getMember().getAsMention()), commandEvent.getChannel(), null);

        ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

        Image im = null;
        try {
            im = ip.getRandomImage("kiss").execute();
        } catch (Exception ignored) {
        }

        Main.getInstance().getCommandManager().sendMessage((im != null ? im.getUrl() : "https://images.ree6.de/notfound.png"), commandEvent.getChannel(), null);
        if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("command.message.default.checkBelow")).queue();
    }
}
