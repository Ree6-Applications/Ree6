package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;

public class TwitterNotifier extends Command {

    public TwitterNotifier() {
        super("twitter", "Twitter Notifier", Category.COMMUNITY);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            sendMessage("This Command doesn't support slash commands yet.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            //TODO add return when finished adding the Twitter API.
        }
    }
}
