package de.presti.ree6.sql.entities.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * SQL Entity for the Twitter-Webhooks.
 */
@Entity
@Table(name = "TwitterNotify")
public class WebhookTwitter extends Webhook {

    /**
     * Name of the User.
     */
    @Column(name = "name")
    private String name;

    /**
     * Constructor.
     */
    public WebhookTwitter() {
    }

    /**
     * Constructor.
     *
     * @param guildId   The guild ID.
     * @param name      The name of the User.
     * @param channelId The channel ID.
     * @param token     The token.
     */
    public WebhookTwitter(String guildId, String name, String channelId, String token) {
        super(guildId, channelId, token);
        this.name = name;
    }

    /**
     * Get the name of the user.
     * @return the username.
     */
    public String getName() {
        return name;
    }
}
