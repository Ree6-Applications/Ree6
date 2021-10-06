package de.presti.ree6.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.ree6.utils.Logger;

import java.util.Objects;

public class Webhook {

    public static void sendWebhook(WebhookMessage message, long channelId, String webhookToken) {
        if (message.getContent() == null || message.getContent().length() < 1 || message.getEmbeds().size() < 1) {
            Logger.log("WebhookMessage", "Failed to send a Webhook Message!");
            Logger.log("WebhookMessage", "Role: " + message.getContent());
            Logger.log("WebhookMessage", "Header: " + (!message.getEmbeds().isEmpty() ? message.getEmbeds().get(0) != null ? message.getEmbeds().get(0).getTitle() != null ? Objects.requireNonNull(message.getEmbeds().get(0).getTitle()).getText() : "null" : "null" : "null"));
        }

        if (webhookToken.equalsIgnoreCase("Error") || channelId == 0) return;
        WebhookClient wcl = WebhookClient.withId(channelId, webhookToken);
        wcl.send(message);
        wcl.close();
    }
}
