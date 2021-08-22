package de.presti.ree6.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;

public class Webhook {

    public static void sendWebhook(WebhookMessage message, long channelId, String webhookToken) {
        WebhookClient wcl = WebhookClient.withId(channelId, webhookToken);
        wcl.send(message);
        wcl.close();
    }

}
