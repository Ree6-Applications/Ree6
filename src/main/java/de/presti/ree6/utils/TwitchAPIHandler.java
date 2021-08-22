package de.presti.ree6.utils;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Main;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TwitchAPIHandler {

    final ArrayList<String> registerChannels = new ArrayList<>();

    private final TwitchClient twitchClient;

    public TwitchAPIHandler() {
        twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).withClientId(Main.config.cfg.getString("twitch.client.id")).withClientSecret(Main.config.cfg.getString("twitch.client.secret")).build();
    }

    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public void registerChannel(String name) {
        twitchClient.getClientHelper().enableStreamEventListener(name.toLowerCase());
        registerChannels.add(name.toLowerCase());
    }

    public void unregisterChannel(String name) {
        twitchClient.getClientHelper().disableStreamEventListener(name.toLowerCase());
        if(isRegisterd(name.toLowerCase())) {
            registerChannels.remove(name.toLowerCase());
        }
    }

    public void registerTwitchLive() {
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {
            String[] credits = Main.sqlWorker.getTwitchNotifyWebhookByName(channelGoLiveEvent.getStream().getUserName().toLowerCase());

            if(credits[0].equalsIgnoreCase("error")) {
                Logger.log("TwitchNotifier", "Error while getting Infos (" + channelGoLiveEvent.getStream().getUserName() + ")");
                return;
            }

            WebhookMessageBuilder wmb = new WebhookMessageBuilder();

            wmb.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wmb.setUsername("Ree6");

            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(channelGoLiveEvent.getStream().getUserName(), null));
            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitch Notifier", null, null));
            webhookEmbedBuilder.setImageUrl(channelGoLiveEvent.getStream().getThumbnailUrl());
            webhookEmbedBuilder.setDescription("Hey the User " + channelGoLiveEvent.getStream().getUserName() + " is now LIVE!\n\nhttps://twitch.tv/" + channelGoLiveEvent.getStream().getUserName() + "\n\nTitle: " + channelGoLiveEvent.getStream().getTitle() +"\nViewercount: " + channelGoLiveEvent.getStream().getViewerCount() + "\nGame: " + channelGoLiveEvent.getStream().getGameName());
            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter("• today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), null));
            webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

            wmb.addEmbeds(webhookEmbedBuilder.build());

            Webhook.sendWebhook(wmb.build(), Long.parseLong(credits[0]), credits[1]);
        });
    }

    public boolean isRegisterd(String name) {
        return registerChannels.contains(name.toLowerCase());
    }
}
