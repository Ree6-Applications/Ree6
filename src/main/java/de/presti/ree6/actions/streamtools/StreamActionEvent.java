package de.presti.ree6.actions.streamtools;

import com.github.philippheuer.events4j.core.domain.Event;
import com.github.twitch4j.common.events.TwitchEvent;
import de.presti.ree6.actions.ActionEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Container class containing the needed Information to run a StreamAction.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreamActionEvent extends ActionEvent {

    /**
     * The related Twitch Event.
     */
    @Nullable Event event;

    /**
     * The Arguments for the Action.
     */
    @Nullable String[] arguments;

    /**
     * Constructor to create a StreamActionEvent.
     * @param guild The related Guild.
     * @param event The related Twitch Event.
     * @param arguments The Arguments for the Action.
     */
    public StreamActionEvent(@NotNull Guild guild, @Nullable Event event, @Nullable String[] arguments) {
        super(guild);
        this.event = event;
        this.arguments = arguments;
    }
}
