package de.presti.ree6.commands;

import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * Command class used to store information and events about and Command.
 */
public abstract class Command {

    // Information like, the name of the command its description and category, and its alias.
    final String name;
    final String description;
    final Category category;
    final String[] alias;

    // CommandData used to add support for SlashCommands
    final CommandData commandData;

    /**
     * Constructor for a simple command.
     * @param command the name of the command.
     * @param description the description of the command.
     * @param category its category.
     */
    public Command(String command, String description, Category category) {
        name = command;
        this.description = description;
        this.category = category;
        this.commandData = new CommandDataImpl(command, description);
        alias = new String[0];
    }

    /**
     * Constructor for a simple command with one or more alias.
     * @param command the name of the command.
     * @param description the description of the command.
     * @param category its category.
     * @param alias the alias of the command.
     */
    public Command(String command, String description, Category category, String[] alias) {
        name = command;
        this.description = description;
        this.category = category;
        this.alias = alias;
        this.commandData = new CommandDataImpl(command, description);
    }

    /**
     * Constructor for a simple command, and with custom {@link CommandData}.
     * @param command the name of the command.
     * @param description the description of the command.
     * @param category its category.
     * @param commandData the custom CommandData
     */
    public Command(String command, String description, Category category, CommandData commandData) {
        name = command;
        this.description = description;
        this.category = category;
        this.commandData = commandData;
        alias = new String[0];
    }

    /**
     * Constructor for a simple command with one or more alias, and with custom {@link CommandData}.
     * @param command the name of the command.
     * @param description the description of the command.
     * @param category its category.
     * @param alias the alias of the command.
     * @param commandData the custom CommandData
     */
    public Command(String command, String description, Category category, String[] alias, CommandData commandData) {
        name = command;
        this.description = description;
        this.category = category;
        this.alias = alias;
        this.commandData = commandData;
    }

    /**
     * Method called when the Command is being performed.
     * @param event the CommandEvent used to get information about the command perform.
     */
    public abstract void onPerform(CommandEvent event);

    /**
     * All aliases of the Command.
     * @return the aliases.
     */
    public String[] getAlias() {
        return alias;
    }

    /**
     * The name of the Command.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * The description of the Command.
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The category of the Command.
     * @return the category.
     */
    public Category getCategory() { return category; }

    /**
     * The CommandData of the Command.
     * @return the CommandData.
     */
    public CommandData getCommandData() {
        return commandData;
    }

    /**
     * Send a message to a special Message-Channel.
     * @param message the Message content.
     * @param messageChannel the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(String message, MessageChannel messageChannel, InteractionHook interactionHook) {
        Main.getInstance().getCommandManager().sendMessage(message, messageChannel, interactionHook);
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     * @param message the Message content.
     * @param messageChannel the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     * @param deleteSecond the delete delay
     */
    public void sendMessage(String message, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        Main.getInstance().getCommandManager().sendMessage(message, deleteSecond, messageChannel, interactionHook);
    }

    /**
     * Send an Embed to a special Message-Channel.
     * @param embedBuilder the Embed content.
     * @param messageChannel the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel, InteractionHook interactionHook) {
        Main.getInstance().getCommandManager().sendMessage(embedBuilder, messageChannel, interactionHook);
    }

    /**
     * Send an Embed to a special Message-Channel, with a deletion delay.
     * @param embedBuilder the Embed content.
     * @param messageChannel the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     * @param deleteSecond the delete delay
     */
    public void sendMessage(EmbedBuilder embedBuilder, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        Main.getInstance().getCommandManager().sendMessage(embedBuilder, deleteSecond, messageChannel, interactionHook);
    }

    /**
     * Delete a specific message.
     * @param message the {@link Message} entity.
     * @param hook the Interaction-hook, if it is a slash event.
     */
    public static void deleteMessage(Message message, InteractionHook hook) {
        Main.getInstance().getCommandManager().deleteMessage(message, hook);
    }

    /**
     * Check if a specific string is an alis of the Command.
     * @param arguments the argument.
     * @return true, if it is an alias | false, if not.
     */
    public boolean isAlias(String arguments) {
        if(getAlias() == null || getAlias().length == 0)
            return false;

        for(String alias : getAlias()) {
            if(alias.equalsIgnoreCase(arguments)) {
                return true;
            }
        }
        return false;
    }
}