package de.presti.ree6.logger;

import net.dv8tion.jda.api.entities.User;

/**
 * This is class is used to store UserData for Logs which work with
 * Data of Users.
 */
public class LoggerUserData {

    // An instance of the Member Entity.
    private final User user;

    /**
     * Constructor for an overall User Interact Event.
     * @param user the User Entity.
     */
    public LoggerUserData(User user) {
        this.user = user;
    }

    /**
     * Retrieve the User associated with this Event.
     * @return the User Entity.
     */
    public User getUser() {
        return user;
    }
}
