package de.presti.ree6.streamtools;

import de.presti.ree6.streamtools.action.IStreamAction;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;

public class StreamActionContainer {

    @Getter(AccessLevel.PUBLIC)
    String twitchChannelId;

    @Getter(AccessLevel.PUBLIC)
    Guild guild;

    @Getter(AccessLevel.PUBLIC)
    String[] arguments;

    HashMap<IStreamAction, String[]> actions = new HashMap<>();

    public void runActions(String userInput) {
        actions.forEach((action, args) -> {

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.contains("%user_input%")) {
                    args[i] = arg.replace("%user_input%", userInput);
                }
            }

            action.runAction(guild, args);
        });
    }
}
