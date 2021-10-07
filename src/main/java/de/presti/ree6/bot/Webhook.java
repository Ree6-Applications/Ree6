package de.presti.ree6.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Logger;

import java.util.Objects;

public class Webhook {

    public static void sendWebhook(WebhookMessage message, long channelId, String webhookToken) {
        if (webhookToken.contains("Not setuped") || channelId == 0) return;
        try(WebhookClient wcl = WebhookClient.withId(channelId, webhookToken)) {
            wcl.send(message);
        } catch (Exception ex) {
            // Main.sqlWorker.deleteLogWebhook(channelId, webhookToken);
            Logger.log("Webhook", "Invalid Webhook: " + channelId + " - " + webhookToken);
            Logger.log("Webhook", ex.getMessage());
        }
    }
}
