package de.presti.ree6.utils.apis;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.TwitchAuth;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.pubsub.PubSubSubscription;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.actions.streamtools.container.StreamActionContainerCreator;
import de.presti.ree6.module.notifications.impl.*;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.TwitchIntegration;
import de.presti.ree6.utils.oauth.DatabaseStorageBackend;
import de.presti.ree6.utils.others.ThreadUtil;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import io.sentry.Sentry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.exceptions.AuthenticationException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;

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
     * Instance of the YouTube Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private YouTubeSonic youTubeSonic;

    /**
     * Instance of the Twitch Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private TwitchSonic twitchSonic;

    /**
     * Instance of the Instagram Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private InstagramSonic instagramSonic;

    /**
     * Instance of the TikTok Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private TikTokSonic tikTokSonic;

    /**
     * Instance of the Twitter Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private TwitterSonic twitterSonic;

    /**
     * Instance of the Reddit Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private RedditSonic redditSonic;

    /**
     * Instance of the RSS Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private RSSSonic rssSonic;

    /**
     * Instance of the Spotify Sonic Manager.
     */
    @Getter(AccessLevel.PUBLIC)
    private SpotifySonic spotifySonic;

    /**
     * A list with all the Twitch Subscription for the Streaming Tools.
     */
    @Getter(AccessLevel.PUBLIC)
    private final HashMap<String, PubSubSubscription[]> twitchSubscription = new HashMap<>();

    /**
     * Constructor used to create instance of the API Clients.
     */
    public Notifier() {
        if (!BotConfig.isModuleActive("notifier")) return;

        log.info("Initializing Twitch Client...");
        twitchSonic = new TwitchSonic();
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

            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new TwitchIntegration(), "FROM TwitchIntegration", null).subscribe(twitchIntegrations -> {
                for (TwitchIntegration twitchIntegration : twitchIntegrations) {
                    OAuth2Credential credential = new OAuth2Credential("twitch", twitchIntegration.getToken());

                    PubSubSubscription[] subscriptions = new PubSubSubscription[2];
                    subscriptions[0] = getTwitchClient().getPubSub().listenForChannelPointsRedemptionEvents(credential, twitchIntegration.getChannelId());
                    subscriptions[1] = getTwitchClient().getPubSub().listenForSubscriptionEvents(credential, twitchIntegration.getChannelId());

                    getTwitchClient().getClientHelper().enableFollowEventListener(twitchIntegration.getChannelId());

                    twitchSubscription.put(credential.getUserId(), subscriptions);
                }
            });

            twitchClient.getEventManager().onEvent(RewardRedeemedEvent.class, event -> StreamActionContainerCreator.getContainers(0).subscribe(list -> list.forEach(container -> {
                if (!event.getRedemption().getChannelId().equalsIgnoreCase(container.getTwitchChannelId()))
                    return;

                if (container.getExtraArgument() == null || event.getRedemption().getReward().getId().equals(container.getExtraArgument())) {
                    container.runActions(event, event.getRedemption().getUserInput());
                }
            })));

            twitchClient.getEventManager().onEvent(FollowEvent.class, event -> StreamActionContainerCreator.getContainers(1).subscribe(list -> list.forEach(container -> {
                if (!event.getChannel().getId().equalsIgnoreCase(container.getTwitchChannelId())) return;

                container.runActions(event, event.getUser().getName());
            })));

            twitchClient.getEventManager().onEvent(ChannelSubscribeEvent.class, event -> StreamActionContainerCreator.getContainers(2).subscribe(list -> list.forEach(container -> {
                if (!event.getBroadcasterUserId().equalsIgnoreCase(container.getTwitchChannelId())) return;

                container.runActions(null, event.getUserName());
            })));
        } catch (Exception exception) {
            Sentry.captureException(exception);
            log.error("Failed to create Twitch Client.", exception);
        }

        log.info("Initializing Twitter Client...");
        twitterSonic = new TwitterSonic();
        try {
            twitterClient = new TwitterClient(TwitterCredentials.builder()
                    .bearerToken(Main.getInstance().getConfig().getConfiguration().getString("twitter.bearer")).build());
        } catch (Exception exception) {
            log.error("Failed to create Twitter Client.", exception);
        }

        log.info("Initializing Reddit Client...");
        redditSonic = new RedditSonic();
        try {
            redditClient = Reddit4J
                    .rateLimited()
                    .setClientId(Main.getInstance().getConfig().getConfiguration().getString("reddit.client.id"))
                    .setClientSecret(Main.getInstance().getConfig().getConfiguration().getString("reddit.client.secret"))
                    .setUserAgent("Ree6Bot/" + BotWorker.getBuild() + " (by /u/PrestiSchmesti)");

            redditClient.userlessConnect();
        } catch (Exception exception) {
            if (exception instanceof AuthenticationException) {
                log.warn("Reddit Credentials are invalid, you can ignore this if you don't use Reddit.");
            } else {
                log.error("Failed to connect to Reddit API.", exception);
                Sentry.captureException(exception);
            }
        }

        log.info("Initializing Instagram Client...");
        instagramSonic = new InstagramSonic();
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
                log.error("Error Message: {}", throwable.getMessage()
                        .replace("com.github.instagram4j.instagram4j.exceptions.IGResponseException: ", "")
                        .replace("&#039;", "'"));
            }
            return null;
        });

        log.info("Initializing Spotify Client...");
        spotifySonic = new SpotifySonic();

        log.info("Initializing Streams...");

        log.info("Creating YouTube Streams...");
        youTubeSonic = new YouTubeSonic();
        ThreadUtil.createThread(x -> youTubeSonic.run(), x -> {
            log.error("Failed to run YouTube Stream!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);

        log.info("Creating Twitter Streams...");
        ThreadUtil.createThread(x -> twitterSonic.run(), x -> {
            log.error("Failed to run Twitter Follower count checker!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);

        log.info("Creating RSS Streams...");
        rssSonic = new RSSSonic();
        ThreadUtil.createThread(x -> rssSonic.run(), x -> {
            log.error("Failed to run RSS Feed Stream!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(3), true, true);

        log.info("Creating TikTok Streams...");
        tikTokSonic = new TikTokSonic();
        ThreadUtil.createThread(x -> tikTokSonic.run(), x -> {
            log.error("Failed to run TikTok Stream!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);

        log.info("Creating Instagram Streams...");
        ThreadUtil.createThread(x -> instagramSonic.run(), x -> {
            log.error("Failed to run Instagram Stream!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);

        log.info("Creating Reddit Streams...");
        ThreadUtil.createThread(x -> redditSonic.run(), x -> {
            log.error("Failed to run Reddit Stream!", x);
            Sentry.captureException(x);
        }, Duration.ofMinutes(5), true, true);

        // Use 1 day instead of minutes, because Spotify release date is at max precise to the day
        log.info("Creating Spotify Streams...");
        ThreadUtil.createThread(x -> spotifySonic.run(), x -> {
            log.error("Failed to run Spotify Stream!", x);
            Sentry.captureException(x);
        }, Duration.ofDays(1), true, true);
    }
}
