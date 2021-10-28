package de.presti.ree6.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Logger;

import java.util.Objects;
import java.util.function.Function;

public class Webhook {

    public static void sendWebhook(WebhookMessage message, long channelId, String webhookToken) {
        if (webhookToken.contains("Not setuped") || channelId == 0) return;
        try(WebhookClient wcl = WebhookClient.withId(channelId, webhookToken)) {
            wcl.send(message).exceptionally(new Function<Throwable, ReadonlyMessage>() {
                @Override
                public ReadonlyMessage apply(Throwable throwable) {
                    if (throwable.getCause() instanceof HttpException) {
                        if (throwable.getMessage().contains("failure 404")) {
                            Main.sqlWorker.deleteLogWebhook(channelId, webhookToken);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            // Main.sqlWorker.deleteLogWebhook(channelId, webhookToken);
            Logger.log("Webhook", "Invalid Webhook: " + channelId + " - " + webhookToken);
            Logger.log("Webhook", ex.getMessage());
        }
    }
}