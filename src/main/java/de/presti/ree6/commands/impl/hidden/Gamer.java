package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;

public class Gamer extends Command {


    public Gamer() {
        super("gamer", "Tell someone that his message is gamer", Category.HIDDEN);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        sendMessage(commandEvent.getMember().getId().equalsIgnoreCase("465586551955652608")  ? "Hello Xasus im really happy to see you here!"
                : commandEvent.getMember().getId().equalsIgnoreCase("836730835268010035") ? "OMG GATONOVA?? NO WAY!"
                : commandEvent.getMember().getId().equalsIgnoreCase("321580743488831490") ? "Lovely Presti nice to see you here im really happy that you are here!"
                : commandEvent.getMember().getId().equalsIgnoreCase("397462379992055819") ? "David! You're like family i love!" : "Who tf are you?", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        commandEvent.getTextChannel().getHistoryBefore(commandEvent.getMessage().getId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/xasus.png").queue();
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
