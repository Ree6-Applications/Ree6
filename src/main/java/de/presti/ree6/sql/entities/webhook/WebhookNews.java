package de.presti.ree6.sql.entities.webhook;

import de.presti.ree6.sql.base.annotations.Table;

/**
 * SQL Entity for the News-Webhooks.
 */
@Table(name = "NewsWebhooks")
public class WebhookNews extends Webhook {

    /**
     * Constructor.
     */
    public WebhookNews() {
    }

    /**
     * @inheritDoc
     */
    public WebhookNews(String guildId, String channelId, String token) {
        super(guildId, channelId, token);
    }
}
