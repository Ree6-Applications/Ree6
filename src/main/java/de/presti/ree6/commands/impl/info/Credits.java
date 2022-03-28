package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;

public class Credits extends CommandClass {

    public Credits() {
        super("credits", "See the Team behind Ree6!", Category.INFO, new String[] { "cred" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        sendMessage("Lead Developer : Presti | 平和#0240\nSupport Developer : xazed | xazed#5014\ndavid. | david.#3120", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
