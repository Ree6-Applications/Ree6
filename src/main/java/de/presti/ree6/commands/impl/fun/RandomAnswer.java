package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.utils.storage.ArrayUtil;
import de.presti.ree6.utils.others.RandomUtils;

public class RandomAnswer extends Command {

    public RandomAnswer() {
        super("8ball", "Let the Magic 8Ball answer your Question!", Category.FUN, new String[] { "answer", "randomanswer" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        sendMessage(ArrayUtil.answers[RandomUtils.random.nextInt((ArrayUtil.answers.length - 1))], commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
