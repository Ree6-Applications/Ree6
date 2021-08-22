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
        sendMessage(sender.getId().equalsIgnoreCase("465586551955652608") ? "Hello Xasus im really happy to see you here!" : sender.getId().equalsIgnoreCase("321580743488831490") ? "Lovely Presti nice to see you here im really happy that you are here!" : sender.getId().equalsIgnoreCase("397462379992055819") ? "David! You're like family i love!" : "Who tf are you?", m, hook);
        m.getHistoryBefore(messageSelf.getId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/xasus").queue();
        deleteMessage(messageSelf);
    }
}
