package de.presti.ree6.utils.apis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.User;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.Webhook;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.ThreadUtil;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;

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

    // Local list of registered YouTube Channels.
    private final ArrayList<String> registeredYouTubeChannels = new ArrayList<>();

    // Local list of registered Twitter Users.
    private final Map<String, TwitterStream> registeredTwitterUsers = new HashMap<>();

    /**
     * Constructor used to created instance of the API Clients.
     */
    public Notifier() {
        twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).withClientId(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.id"))
                .withClientSecret(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.secret")).build();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        if (BotWorker.getVersion() == BotVersion.DEV) configurationBuilder.setDebugEnabled(true);

        configurationBuilder.setOAuthConsumerKey(Main.getInstance().getConfig().getConfiguration().getString("twitter.consumer.key"));
        configurationBuilder.setOAuthConsumerSecret(Main.getInstance().getConfig().getConfiguration().getString("twitter.consumer.secret"));
        configurationBuilder.setOAuthAccessToken(Main.getInstance().getConfig().getConfiguration().getString("twitter.access.key"));
        configurationBuilder.setOAuthAccessTokenSecret(Main.getInstance().getConfig().getConfiguration().getString("twitter.access.secret"));

        twitterClient = new TwitterFactory(configurationBuilder.build()).getInstance();
        createUploadStream();
    }

    //region Twitch

    /**
     * Register a EventHandler for the Twitch Livestream Event.
     */
    public void registerTwitchEventHandler() {
        getTwitchClient().getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {

            // Create Webhook Message.
            WebhookMessageBuilder wmb = new WebhookMessageBuilder();

            wmb.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
            wmb.setUsername("Ree6");

            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(channelGoLiveEvent.getStream().getUserName(), null));
            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitch Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

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
            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
            webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

            wmb.addEmbeds(webhookEmbedBuilder.build());

            // Go through every Webhook that is registered for the Twitch Channel
            for (String[] credits : Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(channelGoLiveEvent.getStream().getUserName().toLowerCase())) {
                Webhook.sendWebhook(null, wmb.build(), Long.parseLong(credits[0]), credits[1], false);
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

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(twitchChannel).isEmpty())
            return;

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
     * Used to Register a Tweet Event for the given Twitter Users
     *
     * @param twitterUsers the Names of the Twitter Users.
     */
    public void registerTwitterUser(List<String> twitterUsers) {
        twitterUsers.forEach(this::registerTwitterUser);
    }

    /**
     * Used to Register a Tweet Event for the given Twitter User
     *
     * @param twitterUser the Name of the Twitter User.
     * @return true, if everything worked out.
     */
    public boolean registerTwitterUser(String twitterUser) {
        if (getTwitterClient() == null) return false;

        twitterUser = twitterUser.toLowerCase();

        twitter4j.User user;

        try {
            user = getTwitterClient().showUser(twitterUser);
            if (user.isProtected()) return false;
        } catch (Exception ignore) {
            return false;
        }

        FilterQuery filterQuery = new FilterQuery();
        filterQuery.follow(user.getId());

        twitter4j.User finalUser = user;
        TwitterStream twitterStream = new TwitterStreamFactory(getTwitterClient().getConfiguration()).getInstance().addListener(new StatusListener() {

            /**
             * Override the onStatus method to inform about a new status.
             * @param status the new Status.
             */
            @Override
            public void onStatus(Status status) {

                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                webhookMessageBuilder.setUsername("Ree6");
                webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());

                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(status.getUser().getName() + " (@" + status.getUser().getScreenName() + ")", null));
                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitter Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                webhookEmbedBuilder.setThumbnailUrl(status.getUser().getBiggerProfileImageURLHttps());

                webhookEmbedBuilder.setDescription(status.getQuotedStatus() != null && !status.isRetweet() ? "**Quoted  " + status.getQuotedStatus().getUser().getScreenName() + "**: " + status.getText() + "\n" :
                        status.getInReplyToScreenName() != null ? "**Reply to " + status.getInReplyToScreenName() + "**: " + status.getText() + "\n" :
                                status.isRetweet() ? "**Retweeted from " + status.getRetweetedStatus().getUser().getScreenName() + "**: " + status.getText().split(": ")[1] + "\n" :
                                        "**" + status.getText() + "**\n");

                if (status.getMediaEntities().length > 0 && status.getMediaEntities()[0].getType().equalsIgnoreCase("photo")) {
                    webhookEmbedBuilder.setImageUrl(status.getMediaEntities()[0].getMediaURLHttps());
                }

                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
                webhookEmbedBuilder.setTimestamp(Instant.now());
                webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                Main.getInstance().getSqlConnector().getSqlWorker().getTwitterWebhooksByName(finalUser.getScreenName()).forEach(strings ->
                        Webhook.sendWebhook(null, webhookMessageBuilder.build(), Long.parseLong(strings[0]),
                                strings[1], false));
            }

            /**
             * No need for this, so just ignore it.
             * @param statusDeletionNotice Data Object.
             */
            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                // Unused
            }

            /**
             * No need for this, so just ignore it.
             * @param numberOfLimitedStatuses Data Object.
             */
            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                // Unused
            }

            /**
             * No need for this, so just ignore it.
             * @param userId Data Object.
             * @param upToStatusId Data Object.
             */
            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                // Unused
            }

            /**
             * No need for this, so just ignore it.
             * @param warning Data Object.
             */
            @Override
            public void onStallWarning(StallWarning warning) {
                // Unused
            }

            /**
             * Inform about an exception.
             * @param ex the Exception
             */
            @Override
            public void onException(Exception ex) {
                Main.getInstance().getLogger().error("[Notifier] Encountered an error, while trying to get the Status update!", ex);
            }
        }).filter(filterQuery);

        if (!isTwitterRegistered(twitterUser)) registeredTwitterUsers.put(twitterUser, twitterStream);

        return true;
    }

    /**
     * Used to Unregister a Tweet Event for the given Twitter User
     *
     * @param twitterUser the Name of the Twitter User.
     */
    public void unregisterTwitterUser(String twitterUser) {
        if (getTwitterClient() == null) return;

        twitterUser = twitterUser.toLowerCase();

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getTwitterWebhooksByName(twitterUser).isEmpty())
            return;

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

    //region YouTube

    /**
     * Used to create a Thread that listens for new YouTube uploads.
     */
    public void createUploadStream() {
        ThreadUtil.createNewThread(x -> {
            try {
                for (String channel : registeredYouTubeChannels) {
                    List<PlaylistItem> playlistItemList = YouTubeAPIHandler.getInstance().getYouTubeUploads(channel);
                    if (!playlistItemList.isEmpty()) {
                        for (PlaylistItem playlistItem : playlistItemList) {
                            PlaylistItemSnippet snippet = playlistItem.getSnippet();
                            DateTime dateTime = snippet.getPublishedAt();
                            if (dateTime != null &&
                                    dateTime.getValue() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()) {
                                PlaylistItemContentDetails contentDetails = playlistItem.getContentDetails();
                                // Create Webhook Message.
                                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                                webhookMessageBuilder.setUsername("Ree6");

                                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(snippet.getChannelTitle(), null));
                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("YouTube Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                                webhookEmbedBuilder.setImageUrl(snippet.getThumbnails().getHigh().getUrl());

                                // Set rest of the Information.
                                webhookEmbedBuilder.setDescription(snippet.getChannelTitle() + " just uploaded a new Video! Check it out <https://www.youtube.com/watch?v=" + contentDetails.getVideoId() + "/> !");
                                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Title**", snippet.getTitle()));
                                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Description**", snippet.getDescription()));
                                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Upload Date**", snippet.getPublishedAt().toStringRfc3339()));
                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
                                webhookEmbedBuilder.setColor(Color.RED.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                Main.getInstance().getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(channel).forEach(strings ->
                                        Webhook.sendWebhook(null, webhookMessageBuilder.build(), Long.parseLong(strings[0]),
                                                strings[1], false));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().error("Couldn't get upload data!", e);

            }
        }, x -> {
            Main.getInstance().getLogger().error("Couldn't start upload Stream!");
        }, Duration.ofMinutes(5), true, true);
    }

    /**
     * Used to register an Upload Event for the given YouTube Channel.
     *
     * @param youtubeChannel the Name of the YouTube Channel.
     */
    public void registerYouTubeChannel(String youtubeChannel) {
        if (getTwitchClient() == null) return;

        youtubeChannel = youtubeChannel.toLowerCase();

        if (!isYouTubeRegistered(youtubeChannel)) registeredYouTubeChannels.add(youtubeChannel);
    }

    /**
     * Used to register an upload Event for the given YouTube Channels.
     *
     * @param youtubeChannels the Names of the YouTube Channels.
     */
    public void registerYouTubeChannel(List<String> youtubeChannels) {
        if (YouTubeAPIHandler.getInstance() == null) return;

        youtubeChannels.forEach(s -> {
            s = s.toLowerCase();
            if (!isYouTubeRegistered(s)) registeredYouTubeChannels.add(s);
        });
    }

    /**
     * Used to unregister an Upload Event for the given YouTube Channel
     *
     * @param youtubeChannel the Name of the YouTube Channel.
     */
    public void unregisterYouTubeChannel(String youtubeChannel) {
        if (YouTubeAPIHandler.getInstance() == null) return;

        youtubeChannel = youtubeChannel.toLowerCase();

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(youtubeChannel).isEmpty())
            return;

        if (isYouTubeRegistered(youtubeChannel)) registeredYouTubeChannels.remove(youtubeChannel);
    }

    /**
     * Check if a YouTube Channel is already being checked.
     *
     * @param youtubeChannel the Name of the YouTube Channel.
     * @return true, if there is an Event for the Channel | false, if there isn't an Event for the Channel.
     */
    public boolean isYouTubeRegistered(String youtubeChannel) {
        return registeredYouTubeChannels.contains(youtubeChannel.toLowerCase());
    }

    //endregion

    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public Twitter getTwitterClient() {
        return twitterClient;
    }
}
