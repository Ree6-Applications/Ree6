package de.presti.ree6.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all action events.
 */
@Setter
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ActionEvent {

    /**
     * The related Guild.
     */
    @NotNull Guild guild;
}
