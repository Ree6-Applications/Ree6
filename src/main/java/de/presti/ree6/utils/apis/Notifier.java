package de.presti.ree6.utils.apis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.feed.FeedIterator;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineImageMedia;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineVideoMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelFollowCountUpdateEvent;
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
import de.presti.ree6.sql.base.entities.SQLResponse;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.entities.webhook.*;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.ThreadUtil;
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.objects.Sorting;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import net.dv8tion.jda.api.entities.GuildChannel;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.io.IOException;
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
        twitchClient = TwitchClientBuilder
                .builder()
                .withEnableHelix(true)
                .withClientId(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.id"))
                .withClientSecret(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.secret"))
                .withEnablePubSub(true)
                .build();

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

        redditClient = Reddit4J
                .rateLimited()
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
            Main.getInstance().getLogger().error("Please input code: ");
            String code = scanner.nextLine();
            scanner.close();
            return code;
        };

        // handler for challenge login
        IGClient.Builder.LoginHandler challengeHandler = (client, response) -> IGChallengeUtils.resolveChallenge(client, response, inputCode);

        instagramClient = IGClient.builder().username(Main.getInstance().getConfig().getConfiguration().getString("instagram.username")).password(Main.getInstance().getConfig().getConfiguration().getString("instagram.password")).onChallenge(challengeHandler).build();
        instagramClient.sendLoginRequest().exceptionally(throwable -> {
            Main.getInstance().getLogger().error("Failed to login to Instagram API.", throwable);
            return null;
        });
        createInstagramPostStream();

        Main.getInstance().getAnalyticsLogger().info("Initializing YouTube Streams...");
        createUploadStream();

        ThreadUtil.createNewThread(x -> {
            for (String twitterName : registeredTwitterUsers.keySet()) {
                SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE twitterFollowerChannelUsername=?", twitterName);
                if (sqlResponse.isSuccess()) {
                    twitter4j.User twitterUser;
                    try {
                        twitterUser = Main.getInstance().getNotifier().getTwitterClient().showUser(twitterName);
                    } catch (TwitterException e) {
                        continue;
                    }

                    List<ChannelStats> channelStats = sqlResponse.getEntities().stream().map(ChannelStats.class::cast).toList();

                    for (ChannelStats channelStat : channelStats) {
                    if (channelStat.getTwitterFollowerChannelUsername() != null) {
                        GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getTwitchFollowerChannelId());
                        String newName = "Twitter Follower: " + twitterUser.getFollowersCount();
                        if (guildChannel != null &&
                                !guildChannel.getName().equalsIgnoreCase(newName)) {
                            guildChannel.getManager().setName(newName).queue();
                        }
                    }
                    }
                }
            }
        }, x -> Main.getInstance().getLogger().error("Failed to run Follower count checker!", x.getCause()), Duration.ofMinutes(5), true, true);
    }

    //region Twitch

    /**
     * Register a EventHandler for the Twitch Livestream Event.
     */
    public void registerTwitchEventHandler() {
        getTwitchClient().getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {

            List<WebhookTwitch> webhooks = Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(channelGoLiveEvent.getChannel().getName());
            if (webhooks.isEmpty()) {
                return;
            }

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
            webhooks.forEach(webhook -> WebhookUtil.sendWebhook(null, wmb.build(), webhook, false));
        });

        getTwitchClient().getEventManager().onEvent(ChannelFollowCountUpdateEvent.class, channelFollowCountUpdateEvent -> {
            SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE LOWER(twitchFollowerChannelUsername) = ?", channelFollowCountUpdateEvent.getChannel().getName());
            if (sqlResponse.isSuccess()) {
                List<ChannelStats> channelStats = sqlResponse.getEntities().stream().map(ChannelStats.class::cast).toList();

                for (ChannelStats channelStat : channelStats) {
                    if (channelStat.getTwitchFollowerChannelId() != null) {
                        GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getTwitchFollowerChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName("Twitch Follower: " + channelFollowCountUpdateEvent.getFollowCount()).queue();
                        }
                    }
                }
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
        getTwitchClient().getClientHelper().enableFollowEventListener(twitchChannel);
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
        getTwitchClient().getClientHelper().enableFollowEventListener(twitchChannels);
    }

    /**
     * Used to Unregister a Livestream Event for the given Twitch Channel
     *
     * @param twitchChannel the Name of the Twitch Channel.
     */
    public void unregisterTwitchChannel(String twitchChannel) {
        if (getTwitchClient() == null) return;

        twitchChannel = twitchChannel.toLowerCase();

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getTwitchWebhooksByName(twitchChannel).isEmpty() ||
                Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE twitchFollowerChannelUsername=?", twitchChannel).isSuccess())
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
     */
    public void registerTwitterUser(String twitterUser) {
        if (getTwitterClient() == null) return;

        twitterUser = twitterUser.toLowerCase();

        twitter4j.User user;

        try {
            user = getTwitterClient().showUser(twitterUser);
            if (user.isProtected()) return;
        } catch (Exception ignore) {
            return;
        }

        FilterQuery filterQuery = new FilterQuery();
        filterQuery.follow(user.getId());

        twitter4j.User finalUser = user;
        TwitterStream twitterStream = new TwitterStreamFactory(getTwitterClient().getConfiguration())
                .getInstance()
                .addListener(new StatusListener() {

                    /**
                     * Override the onStatus method to inform about a new status.
                     * @param status the new Status.
                     */
                    @Override
                    public void onStatus(Status status) {
                        List<WebhookTwitter> webhooks = Main.getInstance().getSqlConnector().getSqlWorker().getTwitterWebhooksByName(finalUser.getScreenName());

                        if (webhooks.isEmpty()) return;

                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                        webhookMessageBuilder.setUsername("Ree6");
                        webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());

                        WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                        webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(status.getUser().getName() + " (@" + status.getUser().getScreenName() + ")", status.getUser().getBiggerProfileImageURLHttps(), null));

                        webhookEmbedBuilder.setDescription(status.getQuotedStatus() != null && !status.isRetweet() ? "**Quoted  " + status.getQuotedStatus().getUser().getScreenName() + "**: " + status.getText() + "\n" : status.getInReplyToScreenName() != null ? "**Reply to " + status.getInReplyToScreenName() + "**: " + status.getText() + "\n" : status.isRetweet() ? "**Retweeted from " + status.getRetweetedStatus().getUser().getScreenName() + "**: " + status.getText().split(": ")[1] + "\n" : status.getText() + "\n");

                        if (status.getMediaEntities().length > 0 && status.getMediaEntities()[0].getType().equalsIgnoreCase("photo")) {
                            webhookEmbedBuilder.setImageUrl(status.getMediaEntities()[0].getMediaURLHttps());
                        }

                        webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
                        webhookEmbedBuilder.setTimestamp(Instant.now());
                        webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                        webhooks.forEach(webhook -> WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
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
                })
                .filter(filterQuery);

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

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getTwitterWebhooksByName(twitterUser).isEmpty() ||
                Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE twitterFollowerChannelUsername=?", twitterUser).isSuccess())
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

                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE youtubeSubscribersChannelUsername=?", channel);
                    if (sqlResponse.isSuccess()) {
                        List<ChannelStats> channelStats = sqlResponse.getEntities().stream().map(ChannelStats.class::cast).toList();

                        com.google.api.services.youtube.model.Channel youTubeChannel;
                        try {
                            youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannel(channel, "snippet, statistics");
                        } catch (IOException e) {
                            return;
                        }

                        for (ChannelStats channelStat : channelStats) {
                            if (channelStat.getYoutubeSubscribersChannelId() != null) {
                                GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getYoutubeSubscribersChannelId());
                                String newName = "YouTube Subscribers: " + (youTubeChannel.getStatistics().getHiddenSubscriberCount() ? "HIDDEN" : youTubeChannel.getStatistics().getSubscriberCount());
                                if (guildChannel != null &&
                                        !guildChannel.getName().equalsIgnoreCase(newName)) {
                                    guildChannel.getManager().setName(newName).queue();
                                }
                            }
                        }
                    }

                    List<WebhookYouTube> webhooks = Main.getInstance().getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(channel);

                    if (webhooks.isEmpty()) return;

                    List<PlaylistItem> playlistItemList = YouTubeAPIHandler.getInstance().getYouTubeUploads(channel);
                    if (!playlistItemList.isEmpty()) {
                        for (PlaylistItem playlistItem : playlistItemList) {
                            PlaylistItemSnippet snippet = playlistItem.getSnippet();
                            DateTime dateTime = snippet.getPublishedAt();
                            if (dateTime != null && dateTime.getValue() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()) {
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

                                webhooks.forEach(webhook -> WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
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

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(youtubeChannel).isEmpty() ||
                Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE youtubeSubscribersChannelUsername=?", youtubeChannel).isSuccess())
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
                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE subredditMemberChannelSubredditName=?", subreddit);

                    if (sqlResponse.isSuccess()) {
                        List<ChannelStats> channelStats = sqlResponse.getEntities().stream().map(ChannelStats.class::cast).toList();

                        RedditSubreddit subredditEntity;
                        try {
                            subredditEntity = Main.getInstance().getNotifier().getSubreddit(subreddit);
                        } catch (IOException | InterruptedException e) {
                            return;
                        }

                        for (ChannelStats channelStat : channelStats) {
                            if (channelStat.getSubredditMemberChannelId() != null) {
                                GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getSubredditMemberChannelId());
                                String newName = "Subreddit Members: " + subredditEntity.getActiveUserCount();
                                if (guildChannel != null &&
                                        !guildChannel.getName().equalsIgnoreCase(newName)) {
                                    guildChannel.getManager().setName(newName).queue();
                                }
                            }
                        }
                    }

                    redditClient.getSubredditPosts(subreddit, Sorting.NEW).submit().stream().filter(redditPost -> redditPost.getCreated() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())).forEach(redditPost -> {
                        List<WebhookReddit> webhooks = Main.getInstance().getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit);

                        if (webhooks.isEmpty()) return;

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

                        webhooks.forEach(webhook -> WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
                    });
                }
            } catch (Exception exception) {
                Main.getInstance().getAnalyticsLogger().error("Could not get Reddit Posts!", exception);
            }
        }, x -> Main.getInstance().getLogger().error("Couldn't start Reddit Stream!"), Duration.ofMinutes(5), true, true);
    }

    /**
     * Used to get a Subreddit.
     *
     * @param subreddit the Name of the Subreddit.
     * @return the Subreddit.
     * @throws IOException          if the Subreddit couldn't be found.
     * @throws InterruptedException if the Thread was interrupted.
     */
    public RedditSubreddit getSubreddit(String subreddit) throws IOException, InterruptedException {
        return redditClient.getSubreddit(subreddit);
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

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit).isEmpty() ||
                Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE subredditMemberChannelSubredditName=?", subreddit).isSuccess()) return;

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

    //region Instagram

    /**
     * Used to register an Instagram-Post Event for all Insta-Users.
     */
    public void createInstagramPostStream() {
        ThreadUtil.createNewThread(x -> {
            for (String username : registeredInstagramUsers) {

                instagramClient.actions().users().findByUsername(username).thenAccept(userAction -> {
                    com.github.instagram4j.instagram4j.models.user.User user = userAction.getUser();

                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE instagramFollowerChannelUsername=?", username);

                    if (sqlResponse.isSuccess()) {
                        List<ChannelStats> channelStats = sqlResponse.getEntities().stream().map(ChannelStats.class::cast).toList();


                        for (ChannelStats channelStat : channelStats) {
                            if (channelStat.getInstagramFollowerChannelId() != null) {
                                GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getInstagramFollowerChannelId());
                                String newName = "Instagram Follower: " + user.getFollower_count();
                                if (guildChannel != null &&
                                        !guildChannel.getName().equalsIgnoreCase(newName)) {
                                    guildChannel.getManager().setName(newName).queue();
                                }
                            }
                        }
                    }

                    List<WebhookInstagram> webhooks = Main.getInstance().getSqlConnector().getSqlWorker().getInstagramWebhookByName(username);

                    if (webhooks.isEmpty()) return;

                    if (!user.is_private()) {
                        FeedIterator<FeedUserRequest, FeedUserResponse> iterable = new FeedIterator<>(instagramClient, new FeedUserRequest(user.getPk()));

                        int limit = 1;
                        while (iterable.hasNext() && limit-- > 0) {
                            FeedUserResponse response = iterable.next();
                            // Actions here
                            response.getItems().stream().filter(post -> post.getTaken_at() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())).forEach(instagramPost -> {
                                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                                webhookMessageBuilder.setUsername("Ree6");

                                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(user.getUsername(), "https://www.instagram.com/" + user.getUsername()));
                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Instagram Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                                // Set rest of the Information.
                                if (instagramPost instanceof TimelineImageMedia timelineImageMedia) {
                                    webhookEmbedBuilder.setImageUrl(timelineImageMedia.getImage_versions2().getCandidates().get(0).getUrl());
                                    webhookEmbedBuilder.setDescription(timelineImageMedia.getCaption().getText());
                                } else if (instagramPost instanceof TimelineVideoMedia timelineVideoMedia) {
                                    webhookEmbedBuilder.setDescription("[Click here to watch the video](" + timelineVideoMedia.getVideo_versions().get(0).getUrl() + ")");
                                } else {
                                    webhookEmbedBuilder.setDescription(user.getUsername() + " just posted something new on Instagram!");
                                }

                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(Data.ADVERTISEMENT, BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                                webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                webhooks.forEach(webhook -> WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), webhook, false));
                            });
                        }
                    }
                }).exceptionally(exception -> {
                    Main.getInstance().getAnalyticsLogger().error("Could not get Instagram User!", exception);
                    return null;
                }).join();
            }
        }, x -> Main.getInstance().getLogger().error("Couldn't start Instagram Stream!"), Duration.ofMinutes(5), true, true);
    }

    /**
     * Used to register an Instagram-Post Event for all Insta-Users.
     *
     * @param username the Names of the User.
     */
    public void registerInstagramUser(String username) {
        if (getInstagramClient() == null) return;

        if (!isInstagramUserRegistered(username)) registeredInstagramUsers.add(username);
    }

    /**
     * Used to register an Instagram-Post Event for all Insta-Users.
     *
     * @param usernames the Names of the Users.
     */
    public void registerInstagramUser(List<String> usernames) {
        if (getInstagramClient() == null) return;

        usernames.forEach(s -> {
            if (!isInstagramUserRegistered(s)) registeredInstagramUsers.add(s);
        });
    }

    /**
     * Used to unregister an Instagram-Post Event for all Insta-Users.
     *
     * @param username the Names of the User.
     */
    public void unregisterInstagramUser(String username) {
        if (getInstagramClient() == null) return;

        if (!Main.getInstance().getSqlConnector().getSqlWorker().getInstagramWebhookByName(username).isEmpty() ||
                Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE instagramFollowerChannelUsername=?", instagramClient).isSuccess()) return;

        if (isInstagramUserRegistered(username)) registeredInstagramUsers.remove(username);
    }

    /**
     * Check if a User is already being checked.
     *
     * @param username the Names of the User.
     * @return true, if there is an Event for the Channel | false, if there isn't an Event for the Channel.
     */
    public boolean isInstagramUserRegistered(String username) {
        return registeredInstagramUsers.contains(username);
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

    /**
     * Get an instance of the InstagramClient.
     *
     * @return instance of the InstagramClient.
     */
    public IGClient getInstagramClient() {
        return instagramClient;
    }
}
