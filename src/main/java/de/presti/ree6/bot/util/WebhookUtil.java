package de.presti.ree6.bot.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
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
        if (webhookToken.contains("Not setup!") || webhookId == 0) return;

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
        try (WebhookClient wcl = WebhookClient.withId(webhookId, webhookToken)) {
            // Send the message and handle exceptions.
            wcl.send(message).exceptionally(throwable -> {
                // If error 404 comes, that means that the webhook is invalid.
                if (throwable.getMessage().contains("failure 404")) {
                    // Inform and delete invalid webhook.
                    switch (typ) {
                        case LOG ->
                                SQLSession.getSqlConnector().getSqlWorker().deleteLogWebhook(webhookId, webhookToken);

                        case WELCOME ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookWelcome(), "FROM WebhookWelcome WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case YOUTUBE ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookYouTube(), "FROM WebhookYouTube WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case TWITTER ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookTwitter(), "FROM WebhookTwitter WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case TWITCH ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookTwitch(), "FROM WebhookTwitch WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case REDDIT ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookReddit(), "FROM WebhookReddit WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case SPOTIFY ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookSpotify(), "FROM WebhookSpotify WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case TIKTOK ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookTikTok(), "FROM WebhookTikTok WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case INSTAGRAM ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new WebhookInstagram(), "FROM WebhookInstagram WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case RSS ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new RSSFeed(), "FROM RSSFeed WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case SCHEDULE ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ScheduledMessage(), "FROM ScheduledMessage WHERE webhookId = :cid AND token = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });

                        case TICKET ->
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE logChannelWebhookId = :cid AND logChannelWebhookToken = :token",
                                        Map.of("cid", String.valueOf(webhookId), "token", webhookToken)).subscribe(x -> {
                                    if (x.isPresent())
                                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get());
                                });
                    }
                    log.error("[Webhook] Deleted invalid Webhook: {} - {}", webhookId, webhookToken);
                } else if (throwable.getMessage().contains("failure 400")) {
                    // If 404 inform that the Message had an invalid Body.
                    log.error("[Webhook] Invalid Body with LogTyp: {}", loggerMessage.getType().name());
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