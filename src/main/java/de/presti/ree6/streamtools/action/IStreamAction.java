package de.presti.ree6.streamtools.action;

import com.github.twitch4j.common.events.TwitchEvent;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to create a StreamAction.
 */
public interface IStreamAction {

    /**
     * Run the specific action.
     * @param guild The guild where the action should be executed.
     * @param twitchEvent The twitch event that was fired.
     * @param arguments Arguments for the action. (Can be null)
     */
    void runAction(@NotNull Guild guild, @Nullable TwitchEvent twitchEvent, @Nullable String[] arguments);
}
