package de.presti.ree6.utils.apis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import cn.hutool.core.exceptions.ValidateException;
import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Image;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.module.itunes.ItunesItem;
import com.apptasticsoftware.rssreader.module.itunes.ItunesRssReader;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.feed.FeedIterator;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineImageMedia;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineVideoMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.TwitchAuth;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.events.ChannelFollowCountUpdateEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.pubsub.PubSubSubscription;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import de.presti.ree6.actions.streamtools.container.StreamActionContainer;
import de.presti.ree6.actions.streamtools.container.StreamActionContainerCreator;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.TwitchIntegration;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.entities.webhook.*;
import de.presti.ree6.utils.data.DatabaseStorageBackend;
import de.presti.ree6.utils.others.ThreadUtil;
import de.presti.wrapper.entities.VideoResult;
import de.presti.wrapper.entities.channel.ChannelResult;
import de.presti.wrapper.tiktok.TikTokWrapper;
import de.presti.wrapper.tiktok.entities.TikTokUser;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.UserV2;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import io.sentry.Sentry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditPost;
import masecla.reddit4j.objects.Sorting;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jsoup.HttpStatusException;

import java.awt.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

// TODO:: translate
// TODO:: fix the Twitter Stream handler, wait for responses via https://github.com/redouane59/twittered/issues/447

/**
 * Utility class used for Event Notifiers. Such as Twitch Livestream, YouTube Upload or Twitter Tweet.
 */
@Slf4j
public class Notifier {

    /**
     * Instance of the Twitch API Client.
     */
    @Getter(AccessLevel.PUBLIC)
    private TwitchClient twitchClient;

    /**
     * Twitch Credential Manager instance.
     */
    @Getter(AccessLevel.PUBLIC)
    CredentialManager credentialManager;

    /**
     * Twitch Identity Provider instance.
     */
    @Getter(AccessLevel.PUBLIC)
    TwitchIdentityProvider twitchIdentityProvider;

    /**
     * Instance of the Twitter API Client.
     */
    @Getter(AccessLevel.PUBLIC)
    private TwitterClient twitterClient;

    /**
     * Instance of the Reddit API Client.
     */
    @Getter(AccessLevel.PUBLIC)
    private Reddit4J redditClient;

    /**
     * Instance of the Instagram API Client.
     */
    @Getter(AccessLevel.PUBLIC)
    private IGClient instagramClient;

    /**
     * Local list of registered Twitch Channels.
     */
    private final ArrayList<String> registeredTwitchChannels = new ArrayList<>();

    /**
     * A list with all the Twitch Subscription for the Streaming Tools.
     */
    @Getter(AccessLevel.PUBLIC)
    private final HashMap<String, PubSubSubscription[]> twitchSubscription = new HashMap<>();

    /**
     * Local list of registered YouTube Channels.
     */
    private final ArrayList<String> registeredYouTubeChannels = new ArrayList<>();
    /**
     * Local list of registered Twitter Users.
     */
    private final ArrayList<String> registeredTwitterUsers = new ArrayList<>();

    /**
     * Local list of registered Subreddits.
     */
    private final ArrayList<String> registeredSubreddits = new ArrayList<>();

    /**
     * Local list of registered Instagram Users.
     */
    private final ArrayList<String> registeredInstagramUsers = new ArrayList<>();

    /**
     * Local list of registered RSS-Feeds.
     */
    private final ArrayList<String> registeredRSSFeeds = new ArrayList<>();

    /**
     * Local list of registered TikTok Users.
     */
    private final ArrayList<Long> registeredTikTokUsers = new ArrayList<>();

    /**
     * Constructor used to created instance of the API Clients.
     */
    public Notifier() {
        if (!BotConfig.isModuleActive("notifier")) return;

        log.info("Initializing Twitch Client...");
        try {
            credentialManager = CredentialManagerBuilder.builder()
                    .withStorageBackend(new DatabaseStorageBackend())
                    .build();

            TwitchAuth.registerIdentityProvider(credentialManager, Main.getInstance().getConfig().getConfiguration().getString("twitch.client.id"),
                    Main.getInstance().getConfig().getConfiguration().getString("twitch.client.secret"), BotConfig.getTwitchAuth(), false);

            twitchIdentityProvider = (TwitchIdentityProvider) credentialManager.getIdentityProviderByName("twitch").orElse(null);

            twitchClient = TwitchClientBuilder
                    .builder()
                    .withEnableHelix(true)
                    .withClientId(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.id"))
                    .withClientSecret(Main.getInstance().getConfig().getConfiguration().getString("twitch.client.secret"))
                    .withEnablePubSub(true)
                    .withCredentialManager(credentialManager)
                    .build();

            for (TwitchIntegration twitchIntegrations :
                    SQLSession.getSqlConnector().getSqlWorker().getEntityList(new TwitchIntegration(), "FROM TwitchIntegration", null)) {

                OAuth2Credential credential = new OAuth2Credential("twitch", twitchIntegrations.getToken());

                PubSubSubscription[] subscriptions = new PubSubSubscription[2];
                subscriptions[0] = getTwitchClient().getPubSub().listenForChannelPointsRedemptionEvents(credential, twitchIntegrations.getChannelId());
                subscriptions[1] = getTwitchClient().getPubSub().listenForSubscriptionEvents(credential, twitchIntegrations.getChannelId());

                getTwitchClient().getClientHelper().enableFollowEventListener(twitchIntegrations.getChannelId());

                twitchSubscription.put(credential.getUserId(), subscriptions);
            }

            twitchClient.getEventManager().onEvent(RewardRedeemedEvent.class, event -> {
                List<StreamActionContainer> list = StreamActionContainerCreator.getContainers(0);
                list.forEach(container -> {
                    if (!event.getRedemption().getChannelId().equalsIgnoreCase(container.getTwitchChannelId())) return;

                    if (container.getExtraArgument() == null || event.getRedemption().getReward().getId().equals(container.getExtraArgument())) {
                        container.runActions(event, event.getRedemption().getUserInput());
                    }
                });
            });

            twitchClient.getEventManager().onEvent(FollowEvent.class, event -> {
                List<StreamActionContainer> list = StreamActionContainerCreator.getContainers(1);
                list.forEach(container -> {
                    if (!event.getChannel().getId().equalsIgnoreCase(container.getTwitchChannelId())) return;

                    container.runActions(event, event.getUser().getName());
                });
            });

            twitchClient.getEventManager().onEvent(ChannelSubscribeEvent.class, event -> {
                List<StreamActionContainer> list = StreamActionContainerCreator.getContainers(2);
                list.forEach(container -> {
                    if (!event.getBroadcasterUserId().equalsIgnoreCase(container.getTwitchChannelId())) return;

                    container.runActions(null, event.getUserName());
                });
            });
        } catch (Exception exception) {
            Sentry.captureException(exception);
            log.error("Failed to create Twitch Client.", exception);
        }

        log.info("Initializing Twitter Client...");

        try {
            twitterClient = new TwitterClient(TwitterCredentials.builder()
                    .bearerToken(Main.getInstance().getConfig().getConfiguration().getString("twitter.bearer")).build());
        } catch (Exception exception) {
            log.error("Failed to create Twitter Client.", exception);
        }

        log.info("Initializing Reddit Client...");

        try {
            redditClient = Reddit4J
                    .rateLimited()
                    .setClientId(Main.getInstance().getConfig().getConfiguration().getString("reddit.client.id"))
                    .setClientSecret(Main.getInstance().getConfig().getConfiguration().getString("reddit.client.secret"))
                    .setUserAgent("Ree6Bot/" + BotWorker.getBuild() + " (by /u/PrestiSchmesti)");

            redditClient.userlessConnect();
            createRedditPostStream();
        } catch (Exception exception) {
            if (exception instanceof AuthenticationException) {
                log.warn("Reddit Credentials are invalid, you can ignore this if you don't use Reddit.");
            } else {
                log.error("Failed to connect to Reddit API.", exception);
                Sentry.captureException(exception);
            }
        }

        log.info("Initializing Instagram Client...");

        // Callable that returns inputted code from System.in
        Callable<String> inputCode = () -> {
            Scanner scanner = new Scanner(System.in);
            log.error("Please input code: ");
            String code = scanner.nextLine();
            scanner.close();
            return code;
        };

        // handler for challenge login
        IGClient.Builder.LoginHandler challengeHandler = (client, response) -> IGChallengeUtils.resolveChallenge(client, response, inputCode);

        instagramClient = IGClient.builder()
                .username(Main.getInstance().getConfig().getConfiguration().getString("instagram.username"))
                .password(Main.getInstance().getConfig().getConfiguration().getString("instagram.password"))
                .onChallenge(challengeHandler).build();

        instagramClient.sendLoginRequest().exceptionally(throwable -> {
            if (BotConfig.isDebug()) {
                log.error("Failed to login to Instagram API, you can ignore this if you don't use Instagram.", throwable);
            } else {
                log.error("Failed to login to Instagram API, you can ignore this if you don't use Instagram.");
                log.error("Error Message: " + throwable.getMessage()
                        .replace("com.github.instagram4j.instagram4j.exceptions.IGResponseException: ", "")
                        .replace("&#039;", "'"));
            }
            return null;
        });
        createInstagramPostStream();

        log.info("Initializing Streams...");

        log.info("Creating YouTube Streams...");
        createYTStream();

        log.info("Creating Twitter Streams...");
        ThreadUtil.createThread(x -> {
            for (String twitterName : registeredTwitterUsers) {
                List<ChannelStats> channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                        "FROM ChannelStats WHERE twitterFollowerChannelUsername=:name", Map.of("name", twitterName));
                if (!channelStats.isEmpty()) {
                    UserV2 twitterUser;
                    try {
                        twitterUser = Main.getInstance().getNotifier().getTwitterClient().getUserFromUserName(twitterName);
                    } catch (NoSuchElementException e) {
                        continue;
                    }

                    if (twitterUser.getData() == null) continue;

                    for (ChannelStats channelStat : channelStats) {
                        if (channelStat.getTwitterFollowerChannelUsername() != null) {
                            GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getTwitchFollowerChannelId());
                            if (guildChannel == null) continue;
                            String newName = LanguageService.getByGuild(guildChannel.getGuild(), "label.twitterCountName", twitterUser.getFollowersCount());

                            if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                continue;

                            if (!guildChannel.getName().equalsIgnoreCase(newName)) {
                                guildChannel.getManager().setName(newName).queue();
                            }
                        }
                    }
                }
            }
        }, x -> {
            log.error("Failed to run Twitter Follower count checker!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);

        log.info("Creating RSS Streams...");
        createRssStream();

        log.info("Creating TikTok Streams...");
        createTikTokStream();
    }

    /**
     * Creates an RSS Stream.
     */
    public void createRssStream() {
        ThreadUtil.createThread(x -> {

            Collection<String> urls = new ArrayList<>(registeredRSSFeeds);

            /*
             *  TODO:: Either switch to RSSHub, YouTubes RSS or stay on API based.
             *  Issue with RSSHub is that it takes 2 hours to update, because of caching.
             *  Issue with YouTube is that it takes over 30 minutes to update, because of idk random internal stuff.
             */

            ////Collection<String> urls = new ArrayList<>(registeredYouTubeChannels.stream().map(c -> "https://rsshub.app/youtube/channel/" + c).toList());

            // TODO:: Wait till Nitter has fixed their RSS Feeds. Or Twitter finally gets the stick out of their ass and stop limiting simple scraping.
            ////urls.addAll(registeredTwitterUsers.stream().map(c -> "https://nitter.net/" + c + "/rss").toList());

            ////urls.addAll(registeredRSSFeeds);

            List<String> checkedIds = new ArrayList<>();

            // To support Podcast RSS.
            new ItunesRssReader()
                    .addItemExtension("media:description", Item::setDescription)
                    .addItemExtension("media:thumbnail", "url", (item, element) -> {
                        Image image = item.getChannel().getImage().orElse(new Image());
                        image.setUrl(element);
                        item.getChannel().setImage(image);
                    }).addItemExtension("media:thumbnail", "width", (item, element) -> {
                        Image image = item.getChannel().getImage().orElse(new Image());
                        image.setWidth(Integer.valueOf(element));
                        item.getChannel().setImage(image);
                    }).addItemExtension("media:thumbnail", "height", (item, element) -> {
                        Image image = item.getChannel().getImage().orElse(new Image());
                        image.setHeight(Integer.valueOf(element));
                        item.getChannel().setImage(image);
                    }).addChannelExtension("published", Channel::setPubDate)
                    .addItemExtension("dc:creator", Item::setAuthor)
                    .addItemExtension("dc:date", Item::setPubDate)
                    .addItemExtension("yt:channelId", Item::setAuthor)
                    .setUserAgent("Ree6Bot/" + BotWorker.getBuild() + " (by Presti)")
                    .read(urls)
                    .sorted()
                    .forEach(item -> {
                        if (item.getPubDate().isEmpty()) return;

                        String typ = "other";

                        if (item.getGuid().isPresent()) {
                            String guid = item.getGuid().get();

                            if (guid.contains("nitter")) {
                                typ = "tw";
                            } else {
                                typ = "other";
                            }
                        }


                        OffsetDateTime dateTime = OffsetDateTime.parse(item.getPubDate().orElse(""), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                        OffsetDateTime now = OffsetDateTime.now();
                        OffsetDateTime threeMinuteAgo = now.minusMinutes(3);

                        if (dateTime.isBefore(threeMinuteAgo)) return;

                        if (item.getChannel() != null) {

                            String id = "";

                            switch (typ) {
                                case "tw" -> id = item.getChannel().getLink().replace("https://nitter.net/", "");

                                case "other" -> id = item.getChannel().getLink();
                            }

                            if (checkedIds.contains(id)) {
                                return;
                            }


                            if (typ.equals("tw")) {
                                List<WebhookTwitter> webhooks = SQLSession.getSqlConnector().getSqlWorker().getTwitterWebhooksByName(item.getChannel().getLink().replace("https://nitter.net/", ""));

                                if (webhooks.isEmpty()) return;

                                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                webhookMessageBuilder.setUsername(BotConfig.getBotName());
                                webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());

                                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                item.getChannel().getImage().ifPresentOrElse(image ->
                                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(),
                                                        URLDecoder.decode(image.getUrl().replace("nitter.net/pic/", ""), StandardCharsets.UTF_8), null)),
                                        () -> webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(), null, null)));


                                webhookEmbedBuilder.setDescription(item.getTitle() + "\n");

                                item.getDescription().ifPresent(description -> {
                                    if (description.contains("<img src=")) {
                                        String imageUrl = description.split("<img src=\"")[1].split("\"")[0];
                                        webhookEmbedBuilder.setImageUrl(imageUrl);
                                    }
                                });

                                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(item.getChannel().getTitle(), null));
                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitter Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl(), null));

                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()));
                                webhookEmbedBuilder.setTimestamp(Instant.now());
                                webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                webhooks.forEach(webhook -> {
                                    String message = webhook.getMessage()
                                            .replace("%name%", item.getChannel().getTitle());

                                    if (item.getLink().isPresent()) {
                                        message = message.replace("%url%", item.getLink().get()
                                                        .replace("nitter.net", "twitter.com"))
                                                .replace("#m", "");
                                    }
                                    webhookMessageBuilder.setContent(message);
                                    WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
                                });
                            } else {
                                try {
                                    List<RSSFeed> webhooks = SQLSession.getSqlConnector().getSqlWorker().getRSSWebhooksByUrl(id);

                                    if (webhooks.isEmpty()) return;

                                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                    webhookMessageBuilder.setUsername(BotConfig.getBotName());
                                    webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());

                                    WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                    item.getChannel().getImage().ifPresentOrElse(image ->
                                                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(),
                                                            URLDecoder.decode(image.getUrl(), StandardCharsets.UTF_8), null)),
                                            () -> webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(), null, null)));

                                    item.getDescription().ifPresent(description -> webhookEmbedBuilder.setDescription(description + "\n"));

                                    if (item instanceof ItunesItem itunesItem) {
                                        webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(itunesItem.getItunesTitle().orElse("No Title"), item.getLink().orElse("No Link")));
                                        itunesItem.getItunesImage().ifPresent(webhookEmbedBuilder::setThumbnailUrl);
                                    } else {

                                        webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(item.getTitle().orElse("No Title"), item.getLink().orElse("No Link")));
                                    }

                                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("RSS Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl(), null));

                                    webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()));
                                    webhookEmbedBuilder.setTimestamp(Instant.now());
                                    webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                                    webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                    webhooks.forEach(webhook -> WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook));
                                } catch (Exception exception) {
                                    Sentry.captureException(exception);
                                }
                            }

                            checkedIds.add(id);
                        }
                    });
        }, Duration.ofMinutes(3), true, true);
    }

    //region Twitch

    /**
     * Register an EventHandler for the Twitch Livestream Event.
     */
    public void registerTwitchEventHandler() {
        getTwitchClient().getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> {

            List<WebhookTwitch> webhooks = SQLSession.getSqlConnector().getSqlWorker().getTwitchWebhooksByName(channelGoLiveEvent.getChannel().getName());
            if (webhooks.isEmpty()) {
                return;
            }

            String twitchUrl = "https://twitch.tv/" + channelGoLiveEvent.getChannel().getName();

            // Create a Webhook Message.
            WebhookMessageBuilder wmb = new WebhookMessageBuilder();

            wmb.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());
            wmb.setUsername(BotConfig.getBotName());

            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(channelGoLiveEvent.getStream().getUserName(), twitchUrl));
            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitch Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl(), null));

            // Try getting the User.
            Optional<User> twitchUserRequest = getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(channelGoLiveEvent.getStream().getUserName())).execute().getUsers().stream().findFirst();
            if (twitchUserRequest.isPresent()) {
                webhookEmbedBuilder.setThumbnailUrl(twitchUserRequest.orElseThrow().getProfileImageUrl());
            }
            webhookEmbedBuilder.setImageUrl(channelGoLiveEvent.getStream().getThumbnailUrl());

            // Set rest of the Information.
            webhookEmbedBuilder.setDescription("**" + channelGoLiveEvent.getStream().getTitle() + "**\n[Watch Stream](" + twitchUrl + ")");
            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Game**", channelGoLiveEvent.getStream().getGameName()));
            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Viewer**", String.valueOf(channelGoLiveEvent.getStream().getViewerCount())));
            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()));
            webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

            wmb.addEmbeds(webhookEmbedBuilder.build());

            // Go through every Webhook that is registered for the Twitch Channel
            webhooks.forEach(webhook -> {
                String message = webhook.getMessage()
                        .replace("%name%", channelGoLiveEvent.getStream().getUserName())
                        .replace("%url%", twitchUrl);
                wmb.setContent(message);
                WebhookUtil.sendWebhook(wmb.build(), webhook);
            });
        });

        getTwitchClient().getEventManager().onEvent(ChannelFollowCountUpdateEvent.class, channelFollowCountUpdateEvent -> {
            List<ChannelStats> channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                    "FROM ChannelStats WHERE LOWER(twitchFollowerChannelUsername) = :name",
                    Map.of("name", channelFollowCountUpdateEvent.getChannel().getName().toLowerCase()));
            if (!channelStats.isEmpty()) {
                for (ChannelStats channelStat : channelStats) {
                    if (channelStat.getTwitchFollowerChannelId() != null) {
                        GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getTwitchFollowerChannelId());
                        if (guildChannel != null) {
                            if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                continue;

                            guildChannel.getManager().setName(LanguageService.getByGuild(guildChannel.getGuild(), "label.twitchCountName", channelFollowCountUpdateEvent.getFollowCount())).queue();
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
            if (s == null) return;

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

        if (!SQLSession.getSqlConnector().getSqlWorker().getTwitchWebhooksByName(twitchChannel).isEmpty() ||
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE twitchFollowerChannelUsername=:name", Map.of("name", twitchChannel)) != null)
            return;

        if (isTwitchRegistered(twitchChannel)) registeredTwitchChannels.remove(twitchChannel);

        getTwitchClient().getClientHelper().disableStreamEventListener(twitchChannel);
        getTwitchClient().getClientHelper().disableFollowEventListener(twitchChannel);
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

        if (!isTwitterRegistered(twitterUser)) {
            registeredTwitterUsers.add(twitterUser);
        }
    }

    /**
     * Used to Unregister a Tweet Event for the given Twitter User
     *
     * @param twitterUser the Name of the Twitter User.
     */
    public void unregisterTwitterUser(String twitterUser) {
        if (getTwitterClient() == null) return;

        twitterUser = twitterUser.toLowerCase();

        if (!SQLSession.getSqlConnector().getSqlWorker().getTwitterWebhooksByName(twitterUser).isEmpty() ||
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE twitterFollowerChannelUsername=:name", Map.of("name", twitterUser)) != null)
            return;

        if (isTwitterRegistered(twitterUser)) {
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
        return registeredTwitterUsers.contains(twitterUser.toLowerCase());
    }

    //endregion

    //region YouTube

    /**
     * Create an API Stream used to update ChannelStats and Notifier of YT.
     */
    public void createYTStream() {
        ThreadUtil.createThread(x -> {
            for (String channel : registeredYouTubeChannels) {
                List<WebhookYouTube> webhooks = SQLSession.getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(channel);

                if (!webhooks.isEmpty()) {
                    try {
                        List<VideoResult> playlistItemList = YouTubeAPIHandler.getInstance().getYouTubeUploads(channel);
                        if (!playlistItemList.isEmpty()) {
                            for (VideoResult playlistItem : playlistItemList) {

                                Main.getInstance().logAnalytic("Video: " + playlistItem.getTitle() + " | " + playlistItem.getUploadDate() + " | " + playlistItem.getActualUploadDate() + " | " + playlistItem.getTimeAgo());
                                Main.getInstance().logAnalytic("Current: " + System.currentTimeMillis() + " | " + (playlistItem.getUploadDate() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()) + " | "
                                        + (playlistItem.getActualUploadDate() != null && playlistItem.getActualUploadDate().before(new Date(System.currentTimeMillis() - Duration.ofDays(2).toMillis()))) + " | " + (playlistItem.getTimeAgo() > 0 && Duration.ofMinutes(5).toMillis() >= playlistItem.getTimeAgo()));

                                if (playlistItem.getUploadDate() != -1 && (playlistItem.getUploadDate() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis() ||
                                        (playlistItem.getTimeAgo() > 0 && Duration.ofMinutes(5).toMillis() >= playlistItem.getTimeAgo())) &&
                                        playlistItem.getActualUploadDate() != null && !playlistItem.getActualUploadDate().before(new Date(System.currentTimeMillis() - Duration.ofDays(2).toMillis()))) {

                                    Main.getInstance().logAnalytic("Passed! -> " + playlistItem.getTitle() + " | " + playlistItem.getUploadDate() + " | " + playlistItem.getActualUploadDate());
                                    // Create a Webhook Message.
                                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                    webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                                    webhookMessageBuilder.setUsername(BotConfig.getBotName());

                                    WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                    webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(playlistItem.getOwnerName(), null));
                                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("YouTube Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                                    webhookEmbedBuilder.setImageUrl(playlistItem.getThumbnail());

                                    webhookEmbedBuilder.setDescription("[**" + playlistItem.getTitle() + "**](https://www.youtube.com/watch?v=" + playlistItem.getId() + ")");

                                    webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
                                    webhookEmbedBuilder.setColor(Color.RED.getRGB());

                                    webhooks.forEach(webhook -> {
                                        String message = webhook.getMessage().replace("%name%", playlistItem.getOwnerName())
                                                .replace("%title%", playlistItem.getTitle())
                                                .replace("%description%", playlistItem.getDescriptionSnippet() != null ? "No Description" : playlistItem.getDescriptionSnippet())
                                                .replace("%url%", "https://www.youtube.com/watch?v=" + playlistItem.getId());

                                        webhookMessageBuilder.setContent(message);
                                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());
                                        WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
                                    });

                                    break;
                                }
                            }
                        }
                    } catch (Exception exception) {
                        Sentry.captureException(exception);
                        log.error("Couldn't get user data of " + channel + "!", exception);
                    }
                }

                List<ChannelStats> channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                        "FROM ChannelStats WHERE youtubeSubscribersChannelUsername=:name", Map.of("name", channel));

                if (!channelStats.isEmpty()) {
                    ChannelResult youTubeChannel;
                    try {
                        // TODO:: change YT Tracker to use the ID instead of username.
                        youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelById(channel);
                    } catch (Exception e) {
                        Sentry.captureException(e);
                        continue;
                    }

                    if (youTubeChannel == null) continue;

                    for (ChannelStats channelStat : channelStats) {
                        if (channelStat.getYoutubeSubscribersChannelId() != null) {
                            GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getYoutubeSubscribersChannelId());

                            if (guildChannel == null) continue;

                            String newName = LanguageService.getByGuild(guildChannel.getGuild(), "label.youtubeCountName", youTubeChannel.getSubscriberCountText());
                            if (!guildChannel.getName().equalsIgnoreCase(newName)) {
                                if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                    continue;

                                guildChannel.getManager().setName(newName).queue();
                            }
                        }
                    }
                }
            }
        }, x -> {
            log.error("Couldn't run YT checker!", x);
            Sentry.captureException(x);
            // Default is 5 minutes.
        }, Duration.ofMinutes(5), true, true);
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

        if (!SQLSession.getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(youtubeChannel).isEmpty() ||
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE youtubeSubscribersChannelUsername=:name", Map.of("name", youtubeChannel)) != null)
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

    public List<RedditPost> getSubredditPosts(String subreddit, Sorting sorting, int limit) throws AuthenticationException, IOException, InterruptedException {
        try {
            return redditClient.getSubredditPosts(subreddit, sorting).limit(limit).submit();
        } catch (ValidateException exception) {
            if (exception.getMessage().startsWith("The parameter")) {
                redditClient.userlessConnect();
                return redditClient.getSubredditPosts(subreddit, sorting).limit(limit).submit();
            }
        }

        return Collections.emptyList();
    }

    /**
     * Used to register a Reddit-Post Event for all Subreddits.
     */
    public void createRedditPostStream() {
        ThreadUtil.createThread(x -> {
            try {
                for (String subreddit : registeredSubreddits) {
                    List<ChannelStats> channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                            "FROM ChannelStats WHERE subredditMemberChannelSubredditName=:name", Map.of("name", subreddit));

                    if (!channelStats.isEmpty()) {
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

                                    if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                        continue;

                                    guildChannel.getManager().setName(newName).queue();
                                }
                            }
                        }
                    }

                    getSubredditPosts(subreddit, Sorting.NEW, 50).stream().filter(redditPost -> redditPost.getCreated() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())).forEach(redditPost -> {
                        List<WebhookReddit> webhooks = SQLSession.getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit);

                        if (webhooks.isEmpty()) return;

                        // Create Webhook Message.
                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                        webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                        webhookMessageBuilder.setUsername(BotConfig.getBotName());

                        WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                        webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(redditPost.getTitle(), redditPost.getUrl()));
                        webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Reddit Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));


                        if (!redditPost.getThumbnail().equalsIgnoreCase("self"))
                            webhookEmbedBuilder.setImageUrl(redditPost.getThumbnail());

                        // Set rest of the Information.
                        webhookEmbedBuilder.setDescription(URLDecoder.decode(redditPost.getSelftext(), StandardCharsets.UTF_8));
                        webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Author**", redditPost.getAuthor()));
                        webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Subreddit**", redditPost.getSubreddit()));
                        webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                        webhookEmbedBuilder.setColor(Color.ORANGE.getRGB());

                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                        webhooks.forEach(webhook -> {
                            String message = webhook.getMessage()
                                    .replace("%title%", redditPost.getTitle())
                                    .replace("%author%", redditPost.getAuthor())
                                    .replace("%name%", redditPost.getSubreddit())
                                    .replace("%url%", redditPost.getUrl());
                            webhookMessageBuilder.setContent(message);
                            WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
                        });
                    });
                }
            } catch (Exception exception) {
                log.error("Could not get Reddit Posts!", exception);
                Sentry.captureException(exception);
            }
        }, x -> {
            log.error("Couldn't start Reddit Stream!");
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);
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

        if (!SQLSession.getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit).isEmpty() ||
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE subredditMemberChannelSubredditName=:name", Map.of("name", subreddit)) != null)
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

    //region Instagram

    /**
     * Used to register an Instagram-Post Event for all Insta-Users.
     */
    public void createInstagramPostStream() {
        ThreadUtil.createThread(x -> {
            if (!instagramClient.isLoggedIn()) return;

            for (String username : registeredInstagramUsers) {

                instagramClient.actions().users().findByUsername(username).thenAccept(userAction -> {
                    com.github.instagram4j.instagram4j.models.user.User user = userAction.getUser();

                    List<ChannelStats> channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                            "FROM ChannelStats WHERE instagramFollowerChannelUsername=:name", Map.of("name", username));

                    if (!channelStats.isEmpty()) {
                        for (ChannelStats channelStat : channelStats) {
                            if (channelStat.getInstagramFollowerChannelId() != null) {
                                GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getInstagramFollowerChannelId());

                                if (guildChannel == null) continue;

                                String newName = LanguageService.getByGuild(guildChannel.getGuild(), "label.instagramCountName", user.getFollower_count());
                                if (!guildChannel.getName().equalsIgnoreCase(newName)) {
                                    if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                        continue;

                                    guildChannel.getManager().setName(newName).queue();
                                }
                            }
                        }
                    }

                    List<WebhookInstagram> webhooks = SQLSession.getSqlConnector().getSqlWorker().getInstagramWebhookByName(username);

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
                                webhookMessageBuilder.setUsername(BotConfig.getBotName());

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

                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                                webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                // TODO:: add this with message.

                                webhooks.forEach(webhook -> WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook));
                            });
                        }
                    }
                }).exceptionally(exception -> {
                    log.error("Could not get Instagram User!", exception);
                    Sentry.captureException(exception);
                    return null;
                }).join();
            }
        }, x -> {
            log.error("Couldn't start Instagram Stream!");
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);
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

        if (!SQLSession.getSqlConnector().getSqlWorker().getInstagramWebhookByName(username).isEmpty() ||
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE instagramFollowerChannelUsername=:name", Map.of("name", username)) != null)
            return;

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

    //region TikTok

    /**
     * Used to create a Thread that handles TikTok notifications.
     */
    public void createTikTokStream() {
        ThreadUtil.createThread(x -> {
            for (long id : registeredTikTokUsers) {
                try {
                    TikTokUser user = TikTokWrapper.getUser(id);

                    List<WebhookTikTok> webhooks = SQLSession.getSqlConnector().getSqlWorker().getTikTokWebhooksByName(user.getId());

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
                } catch (IOException e) {
                    if (e instanceof HttpStatusException httpStatusException) {
                        if (httpStatusException.getStatusCode() == 404) return;
                        // TODO:: check, maybe delete on 404?
                    }

                    Sentry.captureException(e);
                }
            }
        }, Duration.ofMinutes(5), true, true);
    }

    /**
     * Used to register a TikTok User.
     *
     * @param id the ID of the TikTok User.
     */
    public void registerTikTokUser(long id) {
        if (!isTikTokUserRegistered(id)) registeredTikTokUsers.add(id);
    }

    /**
     * Used to register multiple TikTok Users.
     *
     * @param users the ID of the TikTok Users.
     */
    public void registerTikTokUser(List<Long> users) {
        users.forEach(this::registerTikTokUser);
    }

    /**
     * Used to unregister a TikTok User.
     *
     * @param id the ID of the TikTok User.
     */
    public void unregisterTikTokUser(long id) {
        if (isTikTokUserRegistered(id)) registeredTikTokUsers.remove(id);
    }

    /**
     * Check if a TikTok User is already being checked.
     *
     * @param id the Name of the TikTok User.
     * @return true, if there is a User | false, if there isn't a User.
     */
    public boolean isTikTokUserRegistered(long id) {
        return registeredTikTokUsers.contains(id);
    }

    //endregion

    //region RSS

    /**
     * Used to register an RSS Feed.
     *
     * @param rssUrl the Url of the RSS-Feed.
     */
    public void registerRSS(String rssUrl) {
        if (!isRSSRegistered(rssUrl)) registeredRSSFeeds.add(rssUrl);
    }

    /**
     * Used to register an RSS Feed.
     *
     * @param rssUrls the Urls of the RSS-Feeds.
     */
    public void registerRSS(List<String> rssUrls) {
        rssUrls.forEach(s -> {
            if (!isRSSRegistered(s)) registeredRSSFeeds.add(s);
        });
    }

    /**
     * Used to unregister an RSS Feed.
     *
     * @param rssUrl the Url of the RSS-Feed.
     */
    public void unregisterRSS(String rssUrl) {
        if (isRSSRegistered(rssUrl)) registeredRSSFeeds.remove(rssUrl);
    }

    /**
     * Check if an RSS-Feed is already being checked.
     *
     * @param rssUrl the Url of the RSS-Feed.
     * @return true, if there is an Url | false, if there isn't an Url.
     */
    public boolean isRSSRegistered(String rssUrl) {
        return registeredRSSFeeds.contains(rssUrl);
    }

    //endregion
}
