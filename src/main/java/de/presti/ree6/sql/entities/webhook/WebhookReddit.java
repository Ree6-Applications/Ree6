package de.presti.ree6.sql.entities.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * SQL Entity for the Reddit-Webhooks.
 */
@Entity
@Table(name = "RedditNotify")
public class WebhookReddit extends Webhook {

    /**
     * Name of the Channel.
     */
    @Column(name = "subreddit")
    private String subreddit;

    /**
     * Constructor.
     */
    public WebhookReddit() {
    }


    /**
     * Constructor.
     *
     * @param guildId   The guild ID.
     * @param subreddit      The name of the Subreddit.
     * @param channelId The channel ID.
     * @param token     The token.
     */
    public WebhookReddit(String guildId, String subreddit, String channelId, String token) {
        super(guildId, channelId, token);
        this.subreddit = subreddit;
    }

    /**
     * Get the name of the Subreddit.
     * @return the subreddit name.
     */
    public String getSubreddit() {
        return subreddit;
    }
}
