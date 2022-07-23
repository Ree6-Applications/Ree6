package de.presti.ree6.utils.apis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.User;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.ThreadUtil;
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.objects.Sorting;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Utility class used for Event Notifiers. Such as Twitch Livestream, YouTube Upload or Twitter Tweet.
 */
public class Notifier {

    /**
     * Instance of the Twitch API Client.
     */
    private final TwitchClient twitchClient;

    /**
     * Instance of the Twitter API Client.
     */
    private final Twitter twitterClient;

    /**
     * Instance of the Reddit API Client.
     */
    private final Reddit4J redditClient;

    /**
     * Instance of the Instagram API Client.
     */
    private final IGClient instagramClient;

    /**
     * Local list of registered Twitch Channels.
     */
    private final ArrayList<String> registeredTwitchChannels = new ArrayList<>();

    /**
     * Local list of registered YouTube Channels.
     */
    private final ArrayList<String> registeredYouTubeChannels = new ArrayList<>();
    /**
     * Local list of registered Twitter Users.
     */
    private final Map<String, TwitterStream> registeredTwitterUsers = new HashMap<>();

    /**
     * Local list of registered Subreddits.
     */
    private final ArrayList<String> registeredSubreddits = new ArrayList<>();

    /**
     * Local list of registered Instagram Users.
     */
    private final ArrayList<String> registeredInstagramUsers = new ArrayList<>();

    /**
     * Constructor used to created instance of the API Clients.
     */
    public Notifier() {
        Main.getInstance().getAnalyticsLogger().info("Initializing Twitch Client...");
        twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).withClientId(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.id"))
                .withClientSecret(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.secret")).build();

        Main.getInstance().getAnalyticsLogger().info("Initializing Twitter Client...");

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        configurationBuilder
                .setOAuthConsumerKey(Main.getInstance().getConfig().getConfiguration().getString("twitter.consumer.key"))
                .setOAuthConsumerSecret(Main.getInstance().getConfig().getConfiguration().getString("twitter.consumer.secret"))
                .setOAuthAccessToken(Main.getInstance().getConfig().getConfiguration().getString("twitter.access.token"))
                .setOAuthAccessTokenSecret(Main.getInstance().getConfig().getConfiguration().getString("twitter.access.token.secret"))
                .setDebugEnabled(BotWorker.getVersion() == BotVersion.DEVELOPMENT_BUILD);

        twitterClient = new TwitterFactory(configurationBuilder.build()).getInstance();

        Main.getInstance().getAnalyticsLogger().info("Initializing Reddit Client...");

        redditClient = Reddit4J.rateLimited()
                .setClientId(Main.getInstance().getConfig().getConfiguration().getString("reddit.client.id"))
                .setClientSecret(Main.getInstance().getConfig().getConfiguration().getString("reddit.client.secret"))
                .setUserAgent("Ree6Bot/" + BotWorker.getBuild() + " (by /u/PrestiSchmesti)");

        try {
            redditClient.userlessConnect();
            createRedditPostStream();
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Failed to connect to Reddit API.", exception);
        }

        Main.getInstance().getAnalyticsLogger().info("Initializing Instagram Client...");

        // Callable that returns inputted code from System.in
        Callable<String> inputCode = () -> {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please input code: ");
            String code = scanner.nextLine();
            scanner.close();
            return code;
        };

        // handler for challenge login
        IGClient.Builder.LoginHandler challengeHandler = (client, response) -> {
            // included utility to resolve challenges
            // may specify retries. default is 3
            return IGChallengeUtils.resolveChallenge(client, response, inputCode);
        };

        instagramClient = IGClient.builder()
                .username(Main.getInstance().getConfig().getConfiguration().getString("instagram.username"))
                .password(Main.getInstance().getConfig().getConfiguration().getString("instagram.password"))
                .onChallenge(challengeHandler)
                .build();

        Main.getInstance().getAnalyticsLogger().info("Initializing YouTube Streams...");
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
            Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(channelGoLiveEvent.getStream().getUserName().toLowerCase()).forEach(webhook ->
                    WebhookUtil.sendWebhook(null, wmb.build(), webhook, false));
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

                Main.getInstance().getSqlConnector().getSqlWorker().getTwitterWebhooksByName(finalUser.getScreenName()).forEach(webhook ->
                        WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
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
                                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Description**", snippet.getDescription().isEmpty() ? "No Description" : snippet.getDescription()));
                                webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Upload Date**", snippet.getPublishedAt().toStringRfc3339()));
                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
                                webhookEmbedBuilder.setColor(Color.RED.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                Main.getInstance().getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(channel).forEach(webhook ->
                                        WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
                            }
                        }
                    }
                }
            } catch (GoogleJsonResponseException googleJsonResponseException) {
                Main.getInstance().getAnalyticsLogger().error("Encountered an error, while trying to get the YouTube Uploads!", googleJsonResponseException);
            } catch (Exception e) {
                Main.getInstance().getLogger().error("Couldn't get upload data!", e);
            }
        }, x -> Main.getInstance().getLogger().error("Couldn't start upload Stream!"), Duration.ofMinutes(5), true, true);
    }

    /**
     * Used to register an Upload Event for the given YouTube Channel.
     *
     * @param youtubeChannel the Name of the YouTube Channel.
     */
    public void registerYouTubeChannel(String youtubeChannel) {
        if (YouTubeAPIHandler.getInstance() == null) return;

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
        return registeredYouTubeChannels.contains(youtubeChannel);
    }

    //endregion

    //region Reddit

    /**
     * Used to register a Reddit-Post Event for all Subreddits.
     */
    public void createRedditPostStream() {
        ThreadUtil.createNewThread(x -> {
            try {
                for (String subreddit : registeredSubreddits) {
                    redditClient.getSubredditPosts(subreddit, Sorting.NEW).submit().stream().filter(redditPost -> redditPost.getCreated() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())).forEach(redditPost -> {
                        // Create Webhook Message.
                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                        webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                        webhookMessageBuilder.setUsername("Ree6");

                        WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                        webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(redditPost.getTitle(), redditPost.getUrl()));
                        webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Reddit Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));


                        if (!redditPost.getThumbnail().equalsIgnoreCase("self"))
                            webhookEmbedBuilder.setImageUrl(redditPost.getThumbnail());

                        // Set rest of the Information.
                        webhookEmbedBuilder.setDescription(URLDecoder.decode(redditPost.getSelftext(), StandardCharsets.UTF_8));
                        webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Author**", redditPost.getAuthor()));
                        webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Subreddit**", redditPost.getSubreddit()));
                        webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                        webhookEmbedBuilder.setColor(Color.ORANGE.getRGB());

                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                        Main.getInstance().getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit).forEach(webhook ->
                                WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
                    });
                }
            } catch (Exception exception) {
                Main.getInstance().getAnalyticsLogger().error("Could not get Reddit Posts!", exception);
            }
        }, x -> Main.getInstance().getLogger().error("Couldn't start Reddit Stream!"), Duration.ofMinutes(5), true, true);
    }

    /**
     * Used to register a Reddit-Post Event for the given Subreddit
     *
     * @param subreddit the Names of the Subreddit.
     */
    public void registerSubreddit(String subreddit) {
        if (getRedditClient() == null) return;

        if (!isSubredditRegistered(subreddit)) registeredSubreddits.add(subreddit);
    }

    /**
     * Used to register a Reddit-Post Event for the Subreddit.
     *
     * @param subreddits the Names of the Subreddits.
     */
    public void registerSubreddit(List<String> subreddits) {
        if (getRedditClient() == null) return;

        subreddits.forEach(s -> {
            if (!isSubredditRegistered(s)) registeredSubreddits.add(s);
        });
    }

    /**
     * Used to unregister a Reddit-Post Event for the given Subreddit.
     *
     * @param subreddit the Names of the Subreddit.
     */
    public void unregisterSubreddit(String subreddit) {
        if (getRedditClient() == null) return;

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit).isEmpty())
            return;

        if (isSubredditRegistered(subreddit)) registeredSubreddits.remove(subreddit);
    }

    /**
     * Check if a Subreddit is already being checked.
     *
     * @param subreddit the Names of the Subreddit.
     * @return true, if there is an Event for the Channel | false, if there isn't an Event for the Channel.
     */
    public boolean isSubredditRegistered(String subreddit) {
        return registeredSubreddits.contains(subreddit);
    }

    //endregion

    /**
     * Get an instance of the TwitchClient.
     *
     * @return instance of the TwitchClient.
     */
    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    /**
     * Get an instance of the TwitterClient.
     *
     * @return instance of the TwitterClient.
     */
    public Twitter getTwitterClient() {
        return twitterClient;
    }

    /**
     * Get an instance of the RedditClient.
     *
     * @return instance of the RedditClient.
     */
    public Reddit4J getRedditClient() {
        return redditClient;
    }
}
