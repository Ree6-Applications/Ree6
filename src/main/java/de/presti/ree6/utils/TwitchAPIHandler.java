package de.presti.ree6.utils;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.StreamList;
import com.google.common.collect.Lists;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Main;
import rx.Observable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TwitchAPIHandler {

    ArrayList<String> registerChannels = new ArrayList<>();

    private TwitchClient twitchClient;

    public TwitchAPIHandler() {
        twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).withClientId(Main.config.cfg.getString("twitch.client.id")).withClientSecret(Main.config.cfg.getString("twitch.client.secret")).build();
    }

    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public void registerChannel(String name) {
        twitchClient.getClientHelper().enableStreamEventListener(name);
        registerChannels.add(name);
    }

    public void unregisterChannel(String name) {
        twitchClient.getClientHelper().disableStreamEventListener(name);
        if(isRegisterd(name)) {
            registerChannels.remove(name);
        }
    }

    public void registerTwitchLive() {
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {
            String[] credits = Main.sqlWorker.getTwitchNotifyWebhookByName(channelGoLiveEvent.getStream().getUserName());
            WebhookMessageBuilder wmb = new WebhookMessageBuilder();

            wmb.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wmb.setUsername("Ree6");

            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(channelGoLiveEvent.getStream().getUserName(), null, null));
            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Twitch Notifier", null));
            webhookEmbedBuilder.setImageUrl(channelGoLiveEvent.getStream().getThumbnailUrl());
            webhookEmbedBuilder.setDescription("Hey the User " + channelGoLiveEvent.getStream().getUserName() + " is now LIVE!\nhttps://twitch.tv/" + channelGoLiveEvent.getStream().getUserName());
            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter("• today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), null));
            webhookEmbedBuilder.setColor(Color.BLACK.getRGB());

            wmb.addEmbeds(webhookEmbedBuilder.build());

            Webhook.sendWebhook(wmb.build(), Long.parseLong(credits[0]), credits[1]);
        });
    }

    public boolean isRegisterd(String name) {
        return registerChannels.contains(name);
    }
}
