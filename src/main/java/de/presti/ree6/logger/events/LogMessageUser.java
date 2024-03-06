package de.presti.ree6.logger.events;

import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.ree6.logger.LogMessage;
import de.presti.ree6.logger.LogTyp;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

/**
 * Class for overall Interaction handling with Users.
 */
public class LogMessageUser extends LogMessage {

    /**
     * The User Entity.
     */
    private final User user;

    /**
     * Constructor for a Log-Message which shouldn't be handled.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     * @param user            The User related to the Log-Message.
     */
    public LogMessageUser(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp, User user) {
        super(webhookId, webhookAuthCode, webhookMessage, guild, logTyp);
        this.user = user;
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
