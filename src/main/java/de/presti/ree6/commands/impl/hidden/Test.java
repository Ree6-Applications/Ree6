package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.TimeFormat;

public class Test extends Command {

    public Test() {
        super("sdasdwdawrgawhadawrt45646fwng", "test", Category.HIDDEN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        Main.sqlConnector.getSqlWorker().createSettings(m.getGuild().getId());
        for (TimeFormat tm : TimeFormat.values()) {
            sendMessage(tm.now().toString(), 5, m, hook);
        }
        deleteMessage(messageSelf, hook);
    }
}
