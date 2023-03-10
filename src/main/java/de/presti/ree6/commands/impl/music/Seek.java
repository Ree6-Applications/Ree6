package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * Seek to a specific Time in the current Song.
 */
@Command(name = "seek", description = "command.description.seek", category = Category.MUSIC)
public class Seek implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        int seekAmountInSeconds = 1;

        if (commandEvent.isSlashCommand()) {
            OptionMapping optionMapping = commandEvent.getOption("seconds");
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
            commandEvent.reply(commandEvent.getResource("message.music.notConnected"));
            return;
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        Main.getInstance().getMusicWorker().seekInTrack(commandEvent.getChannel(), seekAmountInSeconds);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("seek", LanguageService.getDefault("command.description.seek")).addOptions(new OptionData(OptionType.INTEGER, "seconds", "The seconds that should be seeked (negativ numbers work)").setRequired(true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
