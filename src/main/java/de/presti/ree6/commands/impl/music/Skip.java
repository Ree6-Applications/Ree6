package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;

public class Skip extends CommandClass {

    public Skip() {
        super("skip", "Skip a song!", Category.MUSIC, new String[] { "sk", "next" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            sendMessage("Im not connected to any Channel, so there is nothing to skip!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        Main.getInstance().getMusicWorker().skipTrack(commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
