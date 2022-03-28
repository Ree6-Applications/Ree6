package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;

public class Test extends CommandClass {

    public Test() {
        super("sdasdwdawrgawhadawrt45646fwng", "test", Category.HIDDEN);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        // Nothing to test rn.
    }
}
