package de.presti.ree6.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

/**
 * Event class used to parse and provide Information about a command execution.
 */
public class CommandEvent {

    // The Member associated with the Command execution.
    Member member;

    // The Guild associated with the Command execution.
    Guild guild;

    // The Message that has been parsed.
    Message message;

    // The Text channel which has been used, to fire the Event.
    TextChannel textChannel;

    // Argument that have been given.
    String[] arguments;

    // The Slash Command Event that is associated with the Command execution.
    SlashCommandEvent slashCommandEvent;

    /**
     * Constructor used to save the Data.
     * @param member the {@link Member} Entity.
     * @param guild the {@link Guild} Entity.
     * @param message the {@link Message} Entity.
     * @param textChannel the {@link TextChannel} Entity.
     * @param arguments the given Arguments.
     * @param slashCommandEvent the {@link SlashCommandEvent} Entity.
     */
    public CommandEvent(Member member, Guild guild, Message message, TextChannel textChannel, String[] arguments, SlashCommandEvent slashCommandEvent) {
        this.member = member;
        this.guild = guild;
        this.message = message;
        this.textChannel = textChannel;
        this.arguments = arguments;
        this.slashCommandEvent = slashCommandEvent;
    }

    /**
     * Get the {@link Member} Entity associated with the Event.
     * @return the {@link Member} Entity.
     */
    public Member getMember() {
        return member;
    }

    /**
     * Get the {@link Guild} Entity associated with the Event.
     * @return the {@link Guild} Entity.
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Get the {@link Message} Entity associated with the Event.
     * @return the {@link Message} Entity.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Get the {@link TextChannel} Entity associated with the Event.
     * @return the {@link TextChannel} Entity.
     */
    public TextChannel getTextChannel() {
        return textChannel;
    }

    /**
     * Get the Arguments associated with the Event.
     * @return the Arguments.
     */
    public String[] getArguments() {
        return arguments;
    }

    /**
     * Get the {@link SlashCommandEvent} Entity associated with the Event.
     * @return the {@link SlashCommandEvent} Entity.
     */
    public SlashCommandEvent getSlashCommandEvent() {
        return slashCommandEvent;
    }

    /**
     * Check if the Command Execution is a Slash Command or not.
     * @return true, if it is a Slash Command Execution. | false, if not.
     */
    public boolean isSlashCommand() {
        return getSlashCommandEvent() != null;
    }

    /**
     * Get the {@link InteractionHook} from the {@link SlashCommandEvent}.
     * @return the {@link InteractionHook} Entity.
     */
    public InteractionHook getInteractionHook() {
        if (isSlashCommand()) return getSlashCommandEvent().getHook().setEphemeral(true);

        return null;
    }
}
