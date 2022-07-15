package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "test", description = "test", category = Category.HIDDEN)
public class Test implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        // Nothing to test rn.
        YouTubeAPIHandler.getInstance().addChannelToListener("UCIrF4jV6vQvYxV12c4Ll1YA", playlistItem -> System.out.println(playlistItem.getSnippet().getTitle()));
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
