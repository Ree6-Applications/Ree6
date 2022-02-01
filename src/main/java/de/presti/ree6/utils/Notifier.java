package de.presti.ree6.utils;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.User;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotVersion;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Utility class used for Event Notifiers. Such as Twitch Livestream, YouTube Upload or Twitter Tweet.
 */
public class Notifier {

    // Instance of the Twitch API Client.
    private final TwitchClient twitchClient;

    // Instance of the Twitter API Client.
    private final Twitter twitterClient;

    // Local list of registered Twitch Channels.
    private final ArrayList<String> registeredTwitchChannels = new ArrayList<>();

    // Local list of registered Twitter Users.
    private final Map<String, TwitterStream> registeredTwitterUsers = new HashMap<>();

    /**
     * Constructor used to created instance of the API Clients.
     */
    public Notifier() {
        twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).withClientId(Main.getInstance().getConfig().cfg.getString("twitch.client.id"))
                .withClientSecret(Main.getInstance().getConfig().cfg.getString("twitch.client.secret")).build();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        if (BotInfo.version == BotVersion.DEV) configurationBuilder.setDebugEnabled(true);

        configurationBuilder.setOAuthConsumerKey(Main.getInstance().getConfig().getConfig().getString("twitter.consumer.key"));
        configurationBuilder.setOAuthConsumerSecret(Main.getInstance().getConfig().getConfig().getString("twitter.consumer.secret"));
        configurationBuilder.setOAuthAccessToken(Main.getInstance().getConfig().getConfig().getString("twitter.access.key"));
        configurationBuilder.setOAuthAccessTokenSecret(Main.getInstance().getConfig().getConfig().getString("twitter.access.secret"));

        twitterClient = new TwitterFactory(configurationBuilder.build()).getInstance();
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
                    webhookEmbedBuilder.setImageUrl(twitchUserRequest.orElseThrow().getProfileImageUrl());
                } else {
                    webhookEmbedBuilder.setImageUrl(channelGoLiveEvent.getStream().getThumbnailUrl());
                }

                // Set rest of the Information.
                webhookEmbedBuilder.setDescription(channelGoLiveEvent.getStream().getUserName() + " is now Live on Twitch! Come and join the Stream <https://twitch.tv/" + channelGoLiveEvent.getChannel().getName() + "> !");
                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Title**", channelGoLiveEvent.getStream().getTitle()));
                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Game**", channelGoLiveEvent.getStream().getGameName()));
                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Viewer**", "" + channelGoLiveEvent.getStream().getViewerCount()));
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

        if(!Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(twitchChannel).isEmpty()) return;

        if (isTwitchRegistered(twitchChannel)) registeredTwitchChannels.remove(twitchChannel);

        getTwitchClient().getClientHelper().disableStreamEventListener(twitchChannel);
    }

    /**
     * Check if a Twitch Channel is already being checked.
     *
     * @param twitchChannel the Name of the Twitch Channel.
     * @return true, if there is an Event for the Channel | false, if there isn't an Event for the Channel.
     */
    public boolean isTwitchRegistered(String twitchChannel) {
        return registeredTwitchChannels.contains(twitchChannel.toLowerCase());
    }

    //endregion

    //region Twitter

    /**
     * Used to Register a Tweet Event for the given Twitter User
     *
     * @param twitterUser the Name of the Twitter User.
     */
    public void registerTwitterChannel(String twitterUser) {
        if (getTwitterClient() == null) return;

        twitterUser = twitterUser.toLowerCase();

        TwitterStream twitterStream = new TwitterStreamFactory(getTwitterClient().getConfiguration()).getInstance().user(twitterUser).addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if (!status.getUser().isProtected()) {

                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                    webhookMessageBuilder.setUsername("Ree6");
                    webhookMessageBuilder.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());

                    WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                    webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(status.getUser().getName(), null));
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitter Notifier", BotInfo.botInstance.getSelfUser().getAvatarUrl(), null));

                    webhookEmbedBuilder.setDescription(status.getText());
                    webhookEmbedBuilder.setThumbnailUrl(status.getUser().getProfileImageURL());

                    webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotInfo.botInstance.getSelfUser().getAvatarUrl()));
                    webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                    webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                    Webhook.sendWebhook(null, webhookMessageBuilder.build(), 929416074712219648L, "WeNrLzLD0Oh6ptLNDBRiWt-WP3aU3EC_5_LL7jrKCD0etyHDu4zP48xtLUwOg0FUjuI0");
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {

            }

            @Override
            public void onStallWarning(StallWarning warning) {

            }

            @Override
            public void onException(Exception ex) {

            }
        });

        if (!isTwitterRegistered(twitterUser)) registeredTwitterUsers.put(twitterUser, twitterStream);
    }

    /**
     * Used to Unregister a Tweet Event for the given Twitter User
     *
     * @param twitterUser the Name of the Twitter User.
     */
    public void unregisterTwitterUser(String twitterUser) {
        if (getTwitterClient() == null) return;

        twitterUser = twitterUser.toLowerCase();

        if (isTwitterRegistered(twitterUser)) {

            registeredTwitterUsers.get(twitterUser).cleanUp();

            registeredTwitterUsers.remove(twitterUser);
        }
    }

    /**
     * Check if a Twitter User is already being checked.
     *
     * @param twitterUser the Name of the Twitter User.
     * @return true, if there is an Event for the User | false, if there isn't an Event for the User.
     */
    public boolean isTwitterRegistered(String twitterUser) {
        return registeredTwitterUsers.containsKey(twitterUser.toLowerCase());
    }

    //endregion

    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public Twitter getTwitterClient() {
        return twitterClient;
    }
}
