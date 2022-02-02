package de.presti.ree6.logger.events;

import net.dv8tion.jda.api.entities.User;

/**
 * This is class is used to store UserData for Logs which work with
 * Data of Users.
 */
public record LoggerUserData(User user) {

    /**
     * Constructor for an overall User Interact Event.
     *
     * @param user the User Entity.
     */
    public LoggerUserData {
    }

    /**
     * Retrieve the User associated with this Event.
     *
     * @return the User Entity.
     */
    public User getUser() {
        return user;
    }
}
