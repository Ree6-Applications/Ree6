package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;

public class Skip extends Command {

    public Skip() {
        super("skip", "Skip a song!", Category.MUSIC, new String[] { "sk", "next" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getMusicWorker().skipTrack(commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
