package de.presti.ree6.streamtools.action;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IAction {

    /**
     * Run the specific action.
     * @param guild The guild where the action should be executed.
     * @param arguments Arguments for the action. (Can be null)
     */
    void runAction(@NotNull Guild guild, @Nullable String[] arguments);
}
