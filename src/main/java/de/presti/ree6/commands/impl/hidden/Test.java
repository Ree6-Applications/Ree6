package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Test extends Command {

    public Test() {
        super("sdasdwdawrgawhadawrt45646fwNG", "test", Category.HIDDEN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        //m.sendMessage().queue();
    }
}
