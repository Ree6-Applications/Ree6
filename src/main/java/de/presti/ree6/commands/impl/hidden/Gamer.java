package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "gamer", description = "Tell someone that his message is \"Gamer\"", category = Category.HIDDEN)
public class Gamer implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {

        Main.getInstance().getCommandManager().sendMessage(commandEvent.getMember().getId().equalsIgnoreCase("465586551955652608")  ? "Hello Xasus im really happy to see you here!"
                : commandEvent.getMember().getId().equalsIgnoreCase("836730835268010035") ? "OMG GATONOVA?? NO WAY!"
                : commandEvent.getMember().getId().equalsIgnoreCase("321580743488831490") ? "Lovely Presti nice to see you here im really happy that you are here!"
                : commandEvent.getMember().getId().equalsIgnoreCase("397462379992055819") ? "David! You're like family i love!" : "Who tf are you?", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        commandEvent.getTextChannel().getHistoryBefore(commandEvent.getMessage().getId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/xasus.png").queue();
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
