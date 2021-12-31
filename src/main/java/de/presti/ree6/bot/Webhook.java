package de.presti.ree6.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.ree6.logger.LoggerMessage;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.LoggerImpl;

/**
 * Class to handle Webhook sends.
 */
public class Webhook {

    /**
     * Send a Webhook-message to the wanted Webhook.
     * @param loggerMessage the MessageContent, if it has been merged.
     * @param message the MessageContent.
     * @param webhookId the ID of the Webhook.
     * @param webhookToken the Auth-Token of the Webhook.
     */
    public static void sendWebhook(LoggerMessage loggerMessage, WebhookMessage message, long webhookId, String webhookToken) {

        // Check if the given data is valid.
        if (webhookToken.contains("Not setup!") || webhookId == 0) return;

        // Check if the given data is in the Database.
        if (!Main.getInstance().getSqlConnector().getSqlWorker().existsLogData(webhookId, webhookToken)) return;

        // Check if the LoggerMessage is null or canceled.
        if (loggerMessage == null || loggerMessage.isCanceled()) {
            // If so, inform about invalid send.
            LoggerImpl.log("Webhook", "Got a Invalid or Canceled LoggerMessage!");
            return;
        }

        // Try sending a Webhook to the given data.
        try (WebhookClient wcl = WebhookClient.withId(webhookId, webhookToken)) {
            // Send the message and handle exceptions.
            wcl.send(message).exceptionally(throwable -> {
                // If the error 404 comes that means that the webhook is invalid.
                if (throwable.getMessage().contains("failure 404")) {
                    // Inform and delete invalid webhook.
                    Main.getInstance().getSqlConnector().getSqlWorker().deleteLogWebhook(webhookId, webhookToken);
                    LoggerImpl.log("Webhook", "Deleted invalid Webhook: " + webhookId + " - " + webhookToken);
                } else if (throwable.getMessage().contains("failure 400")) {
                    // If 404 inform that the Message had an invalid Body.
                    LoggerImpl.log("Webhook", "Invalid Body with LogTyp: " + loggerMessage.getType().name());
                }
                return null;
            });
        } catch (Exception ex) {
            // Inform that this is an Invalid Webhook.
            LoggerImpl.log("Webhook", "Invalid Webhook: " + webhookId + " - " + webhookToken);
            LoggerImpl.log("Webhook", ex.getMessage());
        }
    }
}