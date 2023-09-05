package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A Command to manage Giveaways.
 */
@Command(name = "giveaway", description = "command.description.giveaway", category = Category.COMMUNITY)
public class Giveaway implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        switch (commandEvent.getSubcommand()) {
            case "create" -> {
                // TODO:: create.
            }

            case "end" -> {
                // TODO:: end
            }

            case "reroll" -> {
                // TODO:: reroll
            }

            default -> {
                StringBuilder stringBuilder = new StringBuilder("```");
                for (de.presti.ree6.sql.entities.Giveaway giveaway : Main.getInstance().getGiveawayManager().getList()) {
                    stringBuilder.append(commandEvent.getResource("message.giveaway.list.entry", giveaway.getMessageId(), giveaway.getChannelId(), giveaway.getWinners(), giveaway.getPrize(), giveaway.getEnding()));
                }
                stringBuilder.append("```");
                commandEvent.reply(commandEvent.getResource("message.giveaway.list.default") + " " + (stringBuilder.length() == 6 ? "None" : stringBuilder));
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("giveaway", LanguageService.getDefault("command.description.giveaway"))
                .addSubcommands(new SubcommandData("create", "Create a Giveaway.")
                                .addOption(OptionType.STRING, "prize", "The Prize of the Giveaway.", true)
                                .addOption(OptionType.INTEGER, "winners", "The amount of winners.", true)
                                .addOption(OptionType.STRING, "duration", "The duration of the Giveaway.", true),
                        new SubcommandData("end", "End a Giveaway.")
                                .addOption(OptionType.STRING, "id", "The Message ID of the Giveaway.", true),
                        new SubcommandData("reroll", "Reroll a Giveaway.")
                                .addOption(OptionType.STRING, "id", "The Message ID of the Giveaway.", true)
                                .addOption(OptionType.INTEGER, "winners", "The amount of winners.", true),
                        new SubcommandData("list", "List all Giveaways."));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
