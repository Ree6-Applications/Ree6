package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * Skip the current Song.
 */
@Command(name = "skip", description = "command.description.skip", category = Category.MUSIC)
public class Skip implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        int skipAmount = 1;

        if (commandEvent.isSlashCommand()) {
            OptionMapping optionMapping = commandEvent.getSlashCommandInteractionEvent().getOption("amount");
            if (optionMapping != null) {
                skipAmount = optionMapping.getAsInt();
            }
        } else if (commandEvent.getArguments().length >= 1) {
            try {
                skipAmount = Integer.parseInt(commandEvent.getArguments()[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            Main.getInstance().getCommandManager().sendMessage("Im not connected to any Channel, so there is nothing to skip!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        Main.getInstance().getMusicWorker().skipTrack(commandEvent.getChannel(), commandEvent.getInteractionHook(), skipAmount);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("skip", "Skip the current Song.").addOptions(new OptionData(OptionType.INTEGER, "amount", "The amount of songs that should be skipped!").setRequired(false));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "sk", "next" };
    }
}
