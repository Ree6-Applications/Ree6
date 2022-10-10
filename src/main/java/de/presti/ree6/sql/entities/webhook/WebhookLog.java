package de.presti.ree6.sql.entities.webhook;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * SQL Entity for the Log-Webhooks.
 */
@Entity
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
