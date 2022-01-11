package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;

public class TwitterNotifier extends Command {

    public TwitterNotifier() {
        super("twitternotifier", "Twitter Notifier", Category.COMMUNITY);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getNotifier().registerTwitterChannel("memerinoto");
        sendMessage("Notifier started!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
