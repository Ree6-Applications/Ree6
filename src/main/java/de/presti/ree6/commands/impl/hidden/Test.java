package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.utils.TimeFormat;

public class Test extends Command {

    public Test() {
        super("sdasdwdawrgawhadawrt45646fwng", "test", Category.HIDDEN);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getSqlConnector().getSqlWorker().createSettings(commandEvent.getGuild().getId());
        for (TimeFormat tm : TimeFormat.values()) {
            sendMessage(tm.now().toString(), 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
