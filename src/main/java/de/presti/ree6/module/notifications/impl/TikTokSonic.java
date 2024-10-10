package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.wrapper.tiktok.TikTokWrapper;
import de.presti.wrapper.tiktok.entities.TikTokUser;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TikTokSonic implements ISonic {

    ArrayList<SonicIdentifier> tiktokChannels = new ArrayList<>();

    @Override
    public void load() {
        try {
            // Register all TikTok Users.
            SQLSession.getSqlConnector().getSqlWorker().getAllTikTokNames().subscribe(tiktokNames ->
                    tiktokNames.forEach(this::add));
        } catch (Exception exception) {
            log.error("Error while loading TikTok data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public List<SonicIdentifier> getList() {
        return tiktokChannels;
    }

    @Override
    public void run() {
        for (long id : getList().stream().map(SonicIdentifier::getIdentifierAsLong).toList()) {
            try {
                TikTokUser user = TikTokWrapper.getUser(id);

                SQLSession.getSqlConnector().getSqlWorker().getTikTokWebhooksByName(user.getId()).subscribe(webhooks -> {
                    if (webhooks.isEmpty()) {
                        return;
                    }

                    AtomicInteger limit = new AtomicInteger();

                    user.getPosts().forEach(post -> {
                        if (limit.get() > 3) return;

                        if (post.getCreationTime() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())) {
                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                            webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                            webhookMessageBuilder.setUsername(BotConfig.getBotName());

                            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(user.getDisplayName(), "https://www.tiktok.com/@" + user.getName()));
                            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("TikTok Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                            // Set rest of the Information.
                            if (post.getCover() != null) {
                                webhookEmbedBuilder.setImageUrl(post.getCover().getMediumUrl());
                                webhookEmbedBuilder.setDescription("[Click here to watch the video](https://tiktok.com/share/video/" + post.getId() + ")");
                            } else {
                                webhookEmbedBuilder.setDescription(user.getDisplayName() + " just posted something new on TikTok!");
                            }

                            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                            webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

                            webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                            webhooks.forEach(webhook -> {
                                String message = webhook.getMessage()
                                        .replace("%description%", post.getDescription())
                                        .replace("%author%", user.getName())
                                        .replace("%name%", user.getDisplayName())
                                        .replace("%url%", "https://tiktok.com/share/video/" + post.getId());
                                webhookMessageBuilder.setContent(message);
                                WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
                            });
                        }
                        limit.incrementAndGet();
                    });
                });
            } catch (IOException e) {
                if (e instanceof HttpStatusException httpStatusException) {
                    if (httpStatusException.getStatusCode() == 404) return;
                    // TODO:: check, maybe delete on 404?
                }

                Sentry.captureException(e);
            }
        }
    }

    @Override
    public void remove(SonicIdentifier object) {
        if (!contains(object)) return;

        SQLSession.getSqlConnector().getSqlWorker().getTikTokWebhooksByName(object.getIdentifier()).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            tiktokChannels.remove(object);
        });
    }
}
