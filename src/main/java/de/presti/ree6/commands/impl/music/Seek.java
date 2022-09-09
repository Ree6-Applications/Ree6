package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Seek to a specific Time in the current Song.
 */
@Command(name = "seek", description = "Seek to a specific point of a song.", category = Category.MUSIC)
public class Seek implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        int seekAmountInSeconds = 1;

        if (commandEvent.isSlashCommand()) {
            OptionMapping optionMapping = commandEvent.getSlashCommandInteractionEvent().getOption("seconds");
            if (optionMapping != null) {
                seekAmountInSeconds = optionMapping.getAsInt();
            }
        } else if (commandEvent.getArguments().length >= 1) {
            try {
                seekAmountInSeconds = Integer.parseInt(commandEvent.getArguments()[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            Main.getInstance().getCommandManager().sendMessage("Im not connected to any Channel, so there is nothing to skip!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        Main.getInstance().getMusicWorker().seekInTrack(commandEvent.getChannel(), commandEvent.getInteractionHook(), seekAmountInSeconds);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
