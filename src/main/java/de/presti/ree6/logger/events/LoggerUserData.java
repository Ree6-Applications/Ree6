package de.presti.ree6.logger.events;

import net.dv8tion.jda.api.entities.User;

/**
 * Constructor for an overall User Interact Event.
 *
 * @param user the User Entity.
 */
public record LoggerUserData(User user) {

    /**
     * Retrieve the User associated with this Event.
     *
     * @return the User Entity.
     */
    public User getUser() {
        return user;
    }
}
