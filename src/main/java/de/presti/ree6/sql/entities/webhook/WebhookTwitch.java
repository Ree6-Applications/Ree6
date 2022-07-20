package de.presti.ree6.sql.entities.webhook;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;

/**
 * SQL Entity for the Twitch-Webhooks.
 */
@Table(name = "TwitchNotify")
public class WebhookTwitch extends Webhook {

    /**
     * Name of the Channel.
     */
    @Property(name = "name")
    private String name;

    /**
     * Constructor.
     */
    public WebhookTwitch() {
    }


    /**
     * Constructor.
     *
     * @param guildId   The guild ID.
     * @param name      The name of the Channel.
     * @param channelId The channel ID.
     * @param token     The token.
     */
    public WebhookTwitch(String guildId, String name, String channelId, String token) {
        super(guildId, channelId, token);
        this.name = name;
    }

    /**
     * Get the name of the Channel.
     *
     * @return the channel name.
     */
    public String getName() {
        return name;
    }
}
