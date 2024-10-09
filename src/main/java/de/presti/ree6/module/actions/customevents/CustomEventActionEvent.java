package de.presti.ree6.module.actions.customevents;

import de.presti.ree6.module.actions.ActionEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

// TODO:: gotta think about a better naming schema.

/**
 * A Container class containing the needed Information to run a CustomEventAction.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomEventActionEvent extends ActionEvent {

    /**
     * The Arguments for the Action.
     */
    @Nullable String[] arguments;

    /**
     * Constructor.
     * @param guild the related Guild.
     * @param arguments the Arguments used.
     */
    public CustomEventActionEvent(Guild guild, String[] arguments) {
        super(guild);
        this.arguments = arguments;
    }
}
