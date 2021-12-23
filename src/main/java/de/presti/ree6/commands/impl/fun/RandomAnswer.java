package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.RandomUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class RandomAnswer extends Command {

    public RandomAnswer() {
        super("8ball", "Let the Magic 8Ball answer your Question!", Category.FUN, new String[] { "answer", "randomanswer" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        sendMessage(ArrayUtil.answers[RandomUtils.random.nextInt((ArrayUtil.answers.length - 1))], m, hook);
    }
}
