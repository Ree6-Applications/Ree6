package de.presti.ree6.bot.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import okhttp3.OkHttpClient;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.logger.LogMessage;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.ScheduledMessage;
import de.presti.ree6.sql.entities.Tickets;
import de.presti.ree6.sql.entities.webhook.*;
import de.presti.ree6.sql.entities.webhook.base.Webhook;
import de.presti.ree6.sql.entities.webhook.base.WebhookSocial;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class to handle Webhook sends.
 */
@Slf4j
public class WebhookUtil {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private WebhookUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Shared HTTP client for every Webhook send.
     */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * Shared scheduler used by the Webhook clients to honour rate limits.
     */
    private static final ScheduledExecutorService WEBHOOK_SCHEDULER =
            Executors.newScheduledThreadPool(2, runnable -> {
                Thread thread = new Thread(runnable, "Ree6-Webhook");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * Cache of Webhook clients, keyed by Webhook id.
     */
    private static final Map<Long, WebhookClient> CLIENTS = new ConcurrentHashMap<>();

    /**
     * Get (or lazily create) the shared {@link WebhookClient} for the given Webhook.
     *
     * @param webhookId    the ID of the Webhook.
     * @param webhookToken the Auth-Token of the Webhook.
     * @return the {@link WebhookClient}.
     */
    private static WebhookClient getClient(long webhookId, String webhookToken) {
        return CLIENTS.compute(webhookId, (id, existing) -> {
            if (existing != null && !existing.isShutdown()) return existing;

            return new WebhookClientBuilder(id, webhookToken)
                    .setHttpClient(HTTP_CLIENT)
                    .setExecutorService(WEBHOOK_SCHEDULER)
                    .setDaemon(true)
                    .setWait(false)
                    .build();
        });
    }

    /**
     * Drop a cached Webhook client, e.g. once the Webhook turned out to be invalid.
     *
     * @param webhookId the ID of the Webhook.
     */
    private static void invalidateClient(long webhookId) {
        WebhookClient client = CLIENTS.remove(webhookId);
        if (client != null) client.close();
    }

    /**
     * Send a Webhook-message to the wanted Webhook.
     *
     * @param message the MessageContent.
     * @param webhook the Webhook.
     * @param typ     the typ of the Webhook
     */
    public static void sendWebhook(WebhookMessage message, Webhook webhook, WebhookTyp typ) {
        sendWebhook(null, message, webhook.getWebhookId(), webhook.getToken(), typ);
    }

    /**
     * Send a Webhook-message to the wanted Webhook.
     *
     * @param loggerMessage the MessageContent, if it has been merged.
     * @param message       the MessageContent.
     * @param webhook       the Webhook.
     * @param typ           the typ of the Webhook
     */
    public static void sendWebhook(LogMessage loggerMessage, WebhookMessage message, Webhook webhook, WebhookTyp typ) {
        sendWebhook(loggerMessage, message, webhook.getWebhookId(), webhook.getToken(), typ);
    }

    /**
     * Send a Webhook-message to the wanted Webhook.
     *
     * @param message the MessageContent.
     * @param webhook the Webhook.
     * @param typ     the typ of the Webhook
     */
    public static void sendWebhook(WebhookMessage message, WebhookSocial webhook, WebhookTyp typ) {
        sendWebhook(null, message, webhook.getWebhookId(), webhook.getToken(), typ);
    }

    /**
     * Send a Webhook-message to the wanted Webhook.
     *
     * @param loggerMessage the MessageContent, if it has been merged.
     * @param message       the MessageContent.
     * @param webhook       the Webhook.
     * @param typ           the typ of the Webhook
     */
    public static void sendWebhook(LogMessage loggerMessage, WebhookMessage message, WebhookSocial webhook, WebhookTyp typ) {
        sendWebhook(loggerMessage, message, webhook.getWebhookId(), webhook.getToken(), typ);
    }


    /**
     * Send a Webhook-message to the wanted Webhook.
     *
     * @param loggerMessage the MessageContent, if it has been merged.
     * @param message       the MessageContent.
     * @param webhookId     the ID of the Webhook.
     * @param webhookToken  the Auth-Token of the Webhook.
     * @param typ           the typ of the Webhook
     */
    public static void sendWebhook(LogMessage loggerMessage, WebhookMessage message, long webhookId, String webhookToken, WebhookTyp typ) {
        Main.getInstance().logAnalytic("Received a Webhook to send. (Log-Typ: {})", typ == WebhookTyp.LOG ? loggerMessage != null ? loggerMessage.getType().name() : "NONE-LOG" : "NONE-LOG");
        // Check if the given data is valid.
        if (webhookToken == null || webhookToken.contains("Not setup!") || webhookId == 0) return;

        // Check if the given data is in the Database.
        if (typ == WebhookTyp.LOG) {
            SQLSession.getSqlConnector().getSqlWorker().existsLogData(webhookId, webhookToken).subscribe(x -> {
                if (!x) {
                    // If not, inform about invalid send.
                    log.error("[Webhook] Invalid Webhook: {} - {}", webhookId, webhookToken);
                } else {
                    // Check if the LoggerMessage is canceled.
                    if ((loggerMessage == null || loggerMessage.isCanceled())) {
                        // If so, inform about invalid send.
                        log.error("[Webhook] Got a Invalid or canceled LoggerMessage!");
                        return;
                    }

                    sendWebhookMessage(loggerMessage, message, webhookId, webhookToken, typ);
                }
            });
        } else {
            sendWebhookMessage(loggerMessage, message, webhookId, webhookToken, typ);
        }
    }

    private static void sendWebhookMessage(LogMessage loggerMessage, WebhookMessage message, long webhookId, String webhookToken, WebhookTyp typ) {
        // Try sending a Webhook to the given data.
        try {
            WebhookClient wcl = getClient(webhookId, webhookToken);

            // Send the message and handle exceptions.
            wcl.send(message).exceptionally(throwable -> {
                String throwableMessage = throwable.getMessage() == null ? "" : throwable.getMessage();

                // If error 404 comes, that means that the webhook is invalid.
                if (throwableMessage.contains("failure 404")) {
                    // Inform and delete invalid webhook.
                    switch (typ) {
                        case LOG ->
                                SQLSession.getSqlConnector().getSqlWorker().deleteLogWebhook(webhookId, webhookToken);

                        case WELCOME ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookWelcome(), "FROM WebhookWelcome WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookWelcome -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookWelcome).subscribe());
                                });

                        case YOUTUBE ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookYouTube(), "FROM WebhookYouTube WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookYouTube -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookYouTube).subscribe());
                                });

                        case TWITTER ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookTwitter(), "FROM WebhookTwitter WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookTwitter -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookTwitter).subscribe());
                                });

                        case TWITCH ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookTwitch(), "FROM WebhookTwitch WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookTwitch -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookTwitch).subscribe());
                                });

                        case REDDIT ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookReddit(), "FROM WebhookReddit WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookReddit -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookReddit).subscribe());
                                });

                        case SPOTIFY ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookSpotify(), "FROM WebhookSpotify WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookSpotify -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookSpotify).subscribe());
                                });

                        case TIKTOK ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookTikTok(), "FROM WebhookTikTok WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookTikTok -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookTikTok).subscribe());
                                });

                        case INSTAGRAM ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookInstagram(), "FROM WebhookInstagram WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(webhookInstagram -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookInstagram).subscribe());
                                });

                        case RSS ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new RSSFeed(), "FROM RSSFeed WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(rssFeed -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(rssFeed).subscribe());
                                });

                        case SCHEDULE ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ScheduledMessage(), "FROM ScheduledMessage WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(scheduledMessage -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(scheduledMessage).subscribe());
                                });

                        case TICKET ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE logChannelWebhookId = :cid AND logChannelWebhookToken = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    x.ifPresent(tickets -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(tickets).subscribe());
                                });
                    }
                    invalidateClient(webhookId);
                    log.error("[Webhook] Deleted invalid Webhook: {} - {}", webhookId, webhookToken);
                } else if (throwableMessage.contains("failure 400")) {
                    // loggerMessage is null for every non-LOG typ.
                    log.error("[Webhook] Invalid Body with LogTyp: {}",
                            loggerMessage != null ? loggerMessage.getType().name() : typ.name());
                }
                return null;
            });
        } catch (Exception ex) {
            // Inform that this is an Invalid Webhook.
            log.error("[Webhook] Invalid Webhook: {} - {}", webhookId, webhookToken);
            log.error("[Webhook] Exception: ", ex);
        }
    }

    /**
     * Delete a Webhook entry from the Guild.
     *
     * @param guildId       the ID of the Guild.
     * @param webhookEntity the Webhook entity.
     */
    public static void deleteWebhook(long guildId, Webhook webhookEntity) {
        // Get the Guild from the ID.
        Guild guild = BotWorker.getShardManager().getGuildById(guildId);

        if (guild != null) {
            // Delete the existing Webhook.
            guild.retrieveWebhooks()
                    .queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null)
                            .filter(webhook -> webhook.getIdLong() == webhookEntity.getWebhookId() &&
                                    webhook.getToken().equalsIgnoreCase(webhookEntity.getToken()))
                            .forEach(webhook -> webhook.delete().queue()));
        }
    }

    public enum WebhookTyp {
        LOG, WELCOME, TIKTOK, SPOTIFY, YOUTUBE, REDDIT, TWITCH, TWITTER, INSTAGRAM, RSS, SCHEDULE, TICKET
    }
}