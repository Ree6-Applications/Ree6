package de.presti.ree6.utils;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.User;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class used for Event Notifiers. Such as Twitch Livestream, YouTube Upload or Twitter Tweet.
 */
public class Notifier {

    // Instance of the Twitch API Client.
    private final TwitchClient twitchClient;

    // Instance of the Twitter API Client.
    private final Twitter twitterClient;

    private final ArrayList<String> registeredTwitchChannels = new ArrayList<>();

    /**
     * Constructor used to created instance of the API Clients.
     */
    public Notifier() {
        twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).withClientId(Main.getInstance().getConfig().cfg.getString("twitch.client.id"))
                .withClientSecret(Main.getInstance().getConfig().cfg.getString("twitch.client.secret")).build();

        twitterClient = TwitterFactory.getSingleton();
    }

    //region Twitch

    /**
     * Register a EventHandler for the Twitch Livestream Event.
     */
    public void registerTwitchEventHandler() {
        getTwitchClient().getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {

            // Go through every Webhook that is registered for the Twitch Channel
            for (String[] credits : Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(channelGoLiveEvent.getStream().getUserName().toLowerCase())) {

                // Create Webhook Message.
                WebhookMessageBuilder wmb = new WebhookMessageBuilder();

                wmb.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                wmb.setUsername("Ree6");

                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(channelGoLiveEvent.getStream().getUserName(), null));
                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitch Notifier", BotInfo.botInstance.getSelfUser().getAvatarUrl(), null));

                // Try getting the User.
                Optional<User> twitchUserRequest = getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(channelGoLiveEvent.getStream().getUserName())).execute().getUsers().stream().findFirst();
                if (getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(channelGoLiveEvent.getStream().getUserName())).execute().getUsers().stream().findFirst().isPresent()) {
                    webhookEmbedBuilder.setImageUrl(twitchUserRequest.get().getProfileImageUrl());
                } else {
                    webhookEmbedBuilder.setImageUrl(channelGoLiveEvent.getStream().getThumbnailUrl());
                }

                // Set rest of the Information.
                webhookEmbedBuilder.setDescription(channelGoLiveEvent.getStream().getUserName() +" is now Live on Twitch! Come and join the Stream <https://twitch.tv/" + channelGoLiveEvent.getChannel().getName() + "> !");
                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true,"**Title**", channelGoLiveEvent.getStream().getTitle()));
                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true,"**Game**", channelGoLiveEvent.getStream().getGameName()));
                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true,"**Viewer**", "" + channelGoLiveEvent.getStream().getViewerCount()));
                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotInfo.botInstance.getSelfUser().getAvatarUrl()));
                webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

                wmb.addEmbeds(webhookEmbedBuilder.build());

                Webhook.sendWebhook(null, wmb.build(), Long.parseLong(credits[0]), credits[1]);
            }
        });
    }

    /**
     * Used to Register a Livestream Event for the given Twitch Channel
     *
     * @param twitchChannel the Name of the Twitch Channel.
     */
    public void registerTwitchChannel(String twitchChannel) {
        if (getTwitchClient() == null) return;

        twitchChannel = twitchChannel.toLowerCase();

        if (!isTwitchRegistered(twitchChannel)) registeredTwitchChannels.add(twitchChannel);

        getTwitchClient().getClientHelper().enableStreamEventListener(twitchChannel);
    }

    /**
     * Used to Register a Livestream Event for the given Twitch Channels
     *
     * @param twitchChannels the Names of the Twitch Channels.
     */
    public void registerTwitchChannel(List<String> twitchChannels) {
        if (getTwitchClient() == null) return;

        twitchChannels.forEach(s -> {
            s = s.toLowerCase();
            if (!isTwitchRegistered(s)) registeredTwitchChannels.add(s);
        });

        getTwitchClient().getClientHelper().enableStreamEventListener(twitchChannels);
    }

    /**
     * Used to Unregister a Livestream Event for the given Twitch Channel
     *
     * @param twitchChannel the Name of the Twitch Channel.
     */
    public void unregisterTwitchChannel(String twitchChannel) {
        if (getTwitchClient() == null) return;

        twitchChannel = twitchChannel.toLowerCase();

        if (isTwitchRegistered(twitchChannel)) registeredTwitchChannels.remove(twitchChannel);

        getTwitchClient().getClientHelper().disableStreamEventListener(twitchChannel);
    }

    /**
     * Check if a Twitch Channel is already being checked.
     * @param twitchChannel the Name of the Twitch Channel.
     * @return true, if there is an Event for the Channel | false, if there isn't a Event for the Channel.
     */
    public boolean isTwitchRegistered(String twitchChannel) {
        return registeredTwitchChannels.contains(twitchChannel.toLowerCase());
    }

    //endregion

    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public Twitter getTwitterClient() {
        return twitterClient;
    }
}
