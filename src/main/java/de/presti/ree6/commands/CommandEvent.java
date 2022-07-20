package de.presti.ree6.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event class used to parse and provide Information about a command execution.
 */
public class CommandEvent {

    /**
     * The Member associated with the Command execution.
     */
    @Nonnull
    Member member;

    /**
     * The Guild associated with the Command execution.
     */
    @Nonnull
    Guild guild;

    /**
     * The message associated with the Command execution.
     */
    @Nullable
    Message message;

    /**
     * The MessageChannel associated with the Command execution.
     */
    @Nonnull
    MessageChannelUnion channel;

    /**
     * The Arguments parsed with the Command execution.
     */
    @Nullable
    String[] arguments;

    /**
     * The SlashCommandEvent associated with the Command execution.
     */
    @Nullable
    SlashCommandInteractionEvent slashCommandInteractionEvent;

    /**
     * Constructor used to save the Data.
     * @param member the {@link Member} Entity.
     * @param guild the {@link Guild} Entity.
     * @param message the {@link Message} Entity.
     * @param textChannel the {@link TextChannel} Entity.
     * @param arguments the given Arguments.
     * @param slashCommandInteractionEvent the {@link SlashCommandInteractionEvent} Entity.
     */
    public CommandEvent(@Nonnull Member member, @Nonnull Guild guild, @Nullable Message message, @Nonnull MessageChannelUnion textChannel, @Nullable String[] arguments, @Nullable SlashCommandInteractionEvent slashCommandInteractionEvent) {
        this.member = member;
        this.guild = guild;
        this.message = message;
        this.channel = textChannel;
        this.arguments = arguments;
        this.slashCommandInteractionEvent = slashCommandInteractionEvent;
    }

    /**
     * Get the {@link Member} Entity associated with the Event.
     * @return the {@link Member} Entity.
     */
    public @NotNull Member getMember() {
        return member;
    }

    /**
     * Get the {@link Guild} Entity associated with the Event.
     * @return the {@link Guild} Entity.
     */
    public @NotNull Guild getGuild() {
        return guild;
    }

    /**
     * Get the {@link Message} Entity associated with the Event.
     * @return the {@link Message} Entity.
     */
    public @Nullable Message getMessage() {
        return message;
    }

    /**
     * Get the {@link TextChannel} Entity associated with the Event.
     * @return the {@link TextChannel} Entity.
     */
    public @NotNull MessageChannelUnion getChannel() {
        return channel;
    }

    /**
     * Get the Arguments associated with the Event.
     * @return the Arguments.
     */
    public String[] getArguments() {
        if (arguments == null) {
            arguments = new String[0];
        }
        return arguments;
    }

    /**
     * Get the {@link SlashCommandInteractionEvent} Entity associated with the Event.
     * @return the {@link SlashCommandInteractionEvent} Entity.
     */
    public @Nullable SlashCommandInteractionEvent getSlashCommandInteractionEvent() {
        return slashCommandInteractionEvent;
    }

    /**
     * Check if the Command Execution is a Slash Command or not.
     * @return true, if it is a Slash Command Execution. | false, if not.
     */
    public boolean isSlashCommand() {
        return getSlashCommandInteractionEvent() != null;
    }

    /**
     * Get the {@link InteractionHook} from the {@link SlashCommandInteractionEvent}.
     * @return the {@link InteractionHook} Entity.
     */
    public InteractionHook getInteractionHook() {
        if (isSlashCommand()) return getSlashCommandInteractionEvent().getHook().setEphemeral(true);

        return null;
    }
}
