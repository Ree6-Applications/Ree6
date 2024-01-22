package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A command to let the bot response to the last Message with a cringe Image.
 */
@Command(name = "cringe", description = "command.description.cringe", category = Category.FUN)
public class Cringe implements ICommand {

    /**
     * @see ICommand#onPerform(CommandEvent)
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            commandEvent.getChannel().getHistoryBefore(commandEvent.getChannel().getLatestMessageId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/cringe.gif").queue();
            commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.default.checkBelow")).queue();
        } else {
            commandEvent.getChannel().getHistoryBefore(commandEvent.getMessage().getIdLong(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/cringe.gif").queue();
        }

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @see ICommand#getCommandData()
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("shrekimage", LanguageService.getDefault("command.description.cringe_slash"));
    }

    /**
     * @see ICommand#getAlias()
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
