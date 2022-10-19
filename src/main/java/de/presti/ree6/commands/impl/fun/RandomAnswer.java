package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to give you a random response.
 */
@Command(name = "8ball", description = "command.description.8ball", category = Category.FUN)
public class RandomAnswer implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        commandEvent.reply(commandEvent.getResource(ArrayUtil.answers[RandomUtils.random.nextInt((ArrayUtil.answers.length - 1))]));
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
        return new String[] { "answer", "randomanswer" };
    }
}
