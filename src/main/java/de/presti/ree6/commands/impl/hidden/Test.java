package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.data.ArrayUtil;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to test stuff.
 */
@Command(name = "test", description = "command.description.test", category = Category.HIDDEN)
public class Test implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isBotOwner()) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", "BE DEVELOPER"), 5);
            return;
        }

        commandEvent.reply("Contains: " + ArrayUtil.voiceJoined.containsKey(commandEvent.getMember()) + " - " + ArrayUtil.voiceJoined.size() + " - " + ArrayUtil.voiceJoined);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
