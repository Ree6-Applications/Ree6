package de.presti.ree6.sql.entities.webhook;

import de.presti.ree6.sql.base.annotations.Table;

/**
 * SQL Entity for the Log-Webhooks.
 */
@Table(name = "LogWebhooks")
public class WebhookLog extends Webhook {

    /**
     * Constructor.
     */
    public WebhookLog() {
    }

    /**
     * @inheritDoc
     */
    public WebhookLog(String guildId, String channelId, String token) {
        super(guildId, channelId, token);
    }
}
