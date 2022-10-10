package de.presti.ree6.sql.entities.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * SQL Entity for the YouTube-Webhooks.
 */
@Entity
@Table(name = "YouTubeNotify")
public class WebhookYouTube extends Webhook {

    /**
     * Name of the Channel.
     */
    @Column(name = "name")
    private String name;

    /**
     * Constructor.
     */
    public WebhookYouTube() {
    }


    /**
     * Constructor.
     *
     * @param guildId   The guild ID.
     * @param name      The name of the Channel.
     * @param channelId The channel ID.
     * @param token     The token.
     */
    public WebhookYouTube(String guildId, String name, String channelId, String token) {
        super(guildId, channelId, token);
        this.name = name;
    }

    /**
     * Get the name of the Channel.
     * @return the channel name.
     */
    public String getName() {
        return name;
    }
}
