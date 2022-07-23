package de.presti.ree6.sql.entities.webhook;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;

/**
 * SQL Entity for the Instagram-Webhooks.
 */
@Table(name = "InstagramNotify")
public class WebhookInstagram extends Webhook {

    /**
     * Name of the User.
     */
    @Property(name = "name")
    private String name;

    /**
     * Constructor.
     */
    public WebhookInstagram() {
    }

    /**
     * Constructor.
     *
     * @param guildId   The guild ID.
     * @param name      The name of the User.
     * @param channelId The channel ID.
     * @param token     The token.
     */
    public WebhookInstagram(String guildId, String name, String channelId, String token) {
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
