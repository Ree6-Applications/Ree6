package de.presti.ree6.logger;

import club.minnced.discord.webhook.send.WebhookMessage;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Base-class for all Log-Messages.
 */
public class LogMessage {

    /**
     * Webhook ID, used to tell Discord which Webhook should be used.
     */
    private final long id;

    /**
     * Webhook AuthCode, used to authenticate.
     */
    private final String authCode;

    /**
     * Webhook State, this is used to cancel Webhooks that have been merged with others.
     */
    private boolean cancel = false;

    /**
     * The WebhookMessage.
     */
    private WebhookMessage webhookMessage;

    /**
     * LoggerTyp, to know what kind of Log this Message is.
     */
    private LogTyp type;

    /**
     * Webhook Guild, the Guild which fired the Log.
     */
    private final Guild guild;

    /**
     * Constructor for a Log-Message which shouldn't be handled.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     */
    public LogMessage(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp) {
        this.id = webhookId;
        this.authCode = webhookAuthCode;
        this.webhookMessage = webhookMessage;
        this.guild = guild;
        this.type = logTyp;
    }


    /**
     * Get the Guild of the Log-Message.
     *
     * @return the Guild.
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Webhook ID used for Discord to identify which Webhook is meant.
     *
     * @return the Webhook ID.
     */
    public long getId() {
        return id;
    }

    /**
     * The Auth-Code used to authenticate the Webhook Packet.
     *
     * @return the Authentication Code.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Get the current WebhookMessage.
     *
     * @return WebhookMessage.
     */
    public WebhookMessage getWebhookMessage() {
        return webhookMessage;
    }

    /**
     * Change the current Webhook-Message.
     *
     * @param webhookMessage new Webhook-Message.
     */
    public void setWebhookMessage(WebhookMessage webhookMessage) {
        this.webhookMessage = webhookMessage;
    }

    /**
     * The current LogTyp.
     *
     * @return the LogTyp.
     */
    public LogTyp getType() {
        return type;
    }

    /**
     * Change the LogTyp of the current Message.
     *
     * @param type the new LogTyp.
     */
    public void setType(LogTyp type) {
        this.type = type;
    }

    /**
     * Cancel the current Log-Message.
     */
    public void cancel() {
        setCanceled(true);
    }

    /**
     * Check if the Message is canceled or not.
     *
     * @return is the Message canceled.
     */
    public boolean isCanceled() {
        return cancel;
    }

    /**
     * Cancel the LogMessage or "uncancel" the LogMessage.
     *
     * @param cancel should the Message be canceled.
     */
    public void setCanceled(boolean cancel) {
        this.cancel = cancel;
    }
}
