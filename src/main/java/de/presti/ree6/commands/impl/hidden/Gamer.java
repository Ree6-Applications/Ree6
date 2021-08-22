package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Gamer extends Command {


    public Gamer() {
        super("gamer", "Tell someone that his message is gamer", Category.HIDDEN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        sendMessage("Hi!",m,hook);
        m.getHistoryBefore(messageSelf.getId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/xasus.png").queue();
        deleteMessage(messageSelf);
    }
}
