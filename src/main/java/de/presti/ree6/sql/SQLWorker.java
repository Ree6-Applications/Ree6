package de.presti.ree6.sql;

import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.BirthdayWish;
import de.presti.ree6.sql.entities.Blacklist;
import de.presti.ree6.sql.entities.Invite;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.sql.entities.level.VoiceUserLevel;
import de.presti.ree6.sql.entities.roles.AutoRole;
import de.presti.ree6.sql.entities.roles.ChatAutoRole;
import de.presti.ree6.sql.entities.roles.VoiceAutoRole;
import de.presti.ree6.sql.entities.stats.CommandStats;
import de.presti.ree6.sql.entities.stats.GuildCommandStats;
import de.presti.ree6.sql.entities.stats.Statistics;
import de.presti.ree6.sql.entities.webhook.Webhook;
import de.presti.ree6.sql.entities.webhook.WebhookInstagram;
import de.presti.ree6.sql.entities.webhook.WebhookLog;
import de.presti.ree6.sql.entities.webhook.WebhookReddit;
import de.presti.ree6.sql.entities.webhook.WebhookTwitch;
import de.presti.ree6.sql.entities.webhook.WebhookTwitter;
import de.presti.ree6.sql.entities.webhook.WebhookWelcome;
import de.presti.ree6.sql.entities.webhook.WebhookYouTube;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A Class to actually handle the SQL data.
 * Used to provide Data from the Database and to save Data into the Database.
 * <p>
 * Constructor to create a new Instance of the SQLWorker with a ref to the SQL-Connector.
 *
 * @param sqlConnector an Instance of the SQL-Connector to retrieve the data from.
 */
@Slf4j
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve", "unused", "SingleStatementInBlock"})
public record SQLWorker(SQLConnector sqlConnector) {

    //region Level

    //region Chat

    /**
     * Get the Chat XP Count of the give UserID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Long} as XP Count.
     */
    public ChatUserLevel getChatLevelData(String guildId, String userId) {
        if (existsInChatLevel(guildId, userId)) {
            // Return a new UserLevel if there was an error OR if the user isn't in the database.
            return (ChatUserLevel) Objects.requireNonNull(getEntity(ChatUserLevel.class, "SELECT * FROM Level WHERE GID=? AND UID=?", guildId, userId)).getEntity();
        }
        return new ChatUserLevel(guildId, userId, 0);
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInChatLevel(String guildId, String userId) {
        return getEntity(ChatUserLevel.class, "SELECT * FROM Level WHERE GID=? AND UID=?", guildId, userId).isSuccess();
    }

    /**
     * Give the wanted User more XP.
     *
     * @param guildId          the ID of the Guild.
     * @param userLevel        the {@link ChatUserLevel} Entity with all the information.
     */
    public void addChatLevelData(String guildId, @Nonnull ChatUserLevel userLevel) {

        if (isOptOut(guildId, userLevel.getUserId())) {
            return;
        }

        // Check if the User is already saved in the Database.
        if (existsInChatLevel(guildId, userLevel.getUserId())) {

            // If so change the current XP to the new.
            updateEntity(userLevel);
        } else {
            saveEntity(userLevel);
        }
    }

    /**
     * Get the Top list of the Guild Chat XP.
     *
     * @param guildId the ID of the Guild.
     * @param limit   the Limit of how many should be given back.
     * @return {@link List<ChatUserLevel>} as container of the User IDs.
     */
    public List<ChatUserLevel> getTopChat(String guildId, int limit) {
        return Objects.requireNonNull(getEntity(ChatUserLevel.class, "SELECT * FROM Level WHERE GID=? ORDER BY cast(xp as unsigned) DESC LIMIT ?", guildId, limit)).getEntities().stream().map(ChatUserLevel.class::cast).toList();
    }

    /**
     * Get the Top list of the Guild Chat XP.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<String>} as container of the User IDs.
     */
    public List<String> getAllChatLevelSorted(String guildId) {

        // Create the List.
        ArrayList<String> userIds = new ArrayList<>();

        // Creating a SQL Statement to get the Entries from the Level Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM Level WHERE GID=? ORDER BY cast(xp as unsigned) DESC", guildId).getValues("UID").stream().map(String.class::cast).forEach(userIds::add);

        // Return the list.
        return userIds;
    }

    //endregion

    //region Voice

    /**
     * Get the Voice XP Count of the give UserID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link VoiceUserLevel} with information about the User Level.
     */
    public VoiceUserLevel getVoiceLevelData(String guildId, String userId) {
        if (existsInVoiceLevel(guildId, userId)) {
            // Return 0 if there was an error OR if the user isn't in the database.
            return (VoiceUserLevel) Objects.requireNonNull(getEntity(VoiceUserLevel.class, "SELECT * FROM VCLevel WHERE GID=? AND UID=?", guildId, userId)).getEntity();
        }

        return new VoiceUserLevel(guildId, userId, 0);
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInVoiceLevel(String guildId, String userId) {
        return getEntity(VoiceUserLevel.class, "SELECT * FROM VCLevel WHERE GID=? AND UID=?", guildId, userId).isSuccess();
    }

    /**
     * Give the wanted User more XP.
     *
     * @param guildId           the ID of the Guild.
     * @param voiceUserLevel    the {@link VoiceUserLevel} Entity with all the information.
     */
    public void addVoiceLevelData(String guildId, @Nonnull VoiceUserLevel voiceUserLevel) {

        if (isOptOut(guildId, voiceUserLevel.getUserId())) {
            return;
        }

        // Check if the User is already saved in the Database.
        if (existsInVoiceLevel(guildId, voiceUserLevel.getUserId())) {

            // If so change the current XP to the new.
            updateEntity(voiceUserLevel);
        } else {
            saveEntity(voiceUserLevel);
        }
    }

    /**
     * Get the Top list of the Guild Voice XP.
     *
     * @param guildId the ID of the Guild.
     * @param limit   the Limit of how many should be given back.
     * @return {@link List<VoiceUserLevel>} as container of the User IDs.
     */
    public List<VoiceUserLevel> getTopVoice(String guildId, int limit) {
        // Return the list.
        return getEntityList(new VoiceUserLevel(),
                "SELECT * FROM VCLevel WHERE GID=:gid ORDER BY cast(xp as unsigned) DESC LIMIT :limit", Map.of("gid", guildId, "limit", limit));
    }

    /**
     * Get the Top list of the Guild Voice XP.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<String>} as container of the UserIds.
     */
    public List<String> getAllVoiceLevelSorted(String guildId) {

        // Create the List.
        ArrayList<String> userIds = new ArrayList<>();

        // Creating a SQL Statement to get the Entries from the Level Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM VCLevel WHERE GID=? ORDER BY cast(xp as unsigned) DESC", guildId).getValues("UID").stream().map(String.class::cast).forEach(userIds::add);

        // Return the list.
        return userIds;
    }

    //endregion

    //endregion

    //region Webhooks

    //region Logs

    /**
     * Get the LogWebhook data.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Webhook} with all the needed data.
     */
    public Webhook getLogWebhook(String guildId) {
        SQLResponse sqlResponse = getEntity(Webhook.class, "SELECT * FROM LogWebhooks WHERE GID=?", guildId);
        return sqlResponse.isSuccess() ? (Webhook) sqlResponse.getEntity() : null;
    }

    /**
     * Set the LogWebhook in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setLogWebhook(String guildId, String webhookId, String authToken) {

        // Check if there is already a Webhook set.
        if (isLogSetup(guildId)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getLogWebhook(guildId);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM LogWebhooks WHERE GID=?", guildId);
        }

        saveEntity(new WebhookLog(guildId, webhookId, authToken));
    }

    /**
     * Check if the Log Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isLogSetup(String guildId) {
        return getEntity(Webhook.class, "SELECT * FROM LogWebhooks WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if the Log Webhook data is in our Database.
     *
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean existsLogData(long webhookId, String authToken) {
        return getEntity(Webhook.class, "SELECT * FROM LogWebhooks WHERE CID=? AND TOKEN=?", webhookId, authToken).isSuccess();
    }

    /**
     * Set the LogWebhook in our Database.
     *
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     */
    public void deleteLogWebhook(long webhookId, String authToken) {
        // Check if there is a Webhook with this data.
        if (existsLogData(webhookId, authToken)) {

            // Delete if so.
            sqlConnector.querySQL("DELETE FROM LogWebhooks WHERE CID=? AND TOKEN=?", webhookId, authToken);
        }

    }

    //endregion

    //region Welcome

    /**
     * Get the WelcomeWebhooks data.
     *
     * @param guildId the ID of the Guild.
     * @return {@link WebhookWelcome} with all the needed data.
     */
    public WebhookWelcome getWelcomeWebhook(String guildId) {
        SQLResponse sqlResponse = getEntity(WebhookWelcome.class, "SELECT * FROM WelcomeWebhooks WHERE GID=?", guildId);
        return sqlResponse.isSuccess() ? (WebhookWelcome) sqlResponse.getEntity() : null;
    }

    /**
     * Set the WelcomeWebhooks in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setWelcomeWebhook(String guildId, String webhookId, String authToken) {

        // Check if there is already a Webhook set.
        if (isWelcomeSetup(guildId)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getWelcomeWebhook(guildId);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM WelcomeWebhooks WHERE GID=?", guildId);
        }

        saveEntity(new WebhookWelcome(guildId, webhookId, authToken));

    }

    /**
     * Check if the Welcome Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isWelcomeSetup(String guildId) {
        return getEntity(WebhookWelcome.class, "SELECT * FROM WelcomeWebhooks WHERE GID=?", guildId).isSuccess();
    }

    //endregion

    //region Twitch Notifier

    /**
     * Get the TwitchNotify data.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link WebhookTwitch} with all the needed data.
     */
    public WebhookTwitch getTwitchWebhook(String guildId, String twitchName) {
        SQLResponse sqlResponse = getEntity(WebhookTwitch.class, "SELECT * FROM TwitchNotify WHERE GID=? AND NAME=?", guildId, twitchName);
        return sqlResponse.isSuccess() ? (WebhookTwitch) sqlResponse.getEntity() : null;
    }

    /**
     * Get the TwitchNotify data.
     *
     * @param twitchName the Username of the Twitch User.
     * @return {@link List<WebhookTwitch>} with all the needed data.
     */
    public List<WebhookTwitch> getTwitchWebhooksByName(String twitchName) {
        SQLResponse sqlResponse = getEntity(WebhookTwitch.class, "SELECT * FROM TwitchNotify WHERE NAME=?", twitchName);
        return sqlResponse.isSuccess() ? sqlResponse.getEntities().stream().map(WebhookTwitch.class::cast).toList() : new ArrayList<>();
    }

    /**
     * Get the all Twitch-Notifier.
     *
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllTwitchNames() {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the TwitchNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM TwitchNotify").getValues("NAME").stream().map(String.class::cast).forEach(userNames::add);

        return userNames;
    }

    /**
     * Get every Twitch-Notifier that has been set up for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllTwitchNames(String guildId) {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the TwitchNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM TwitchNotify WHERE GID=?", guildId).getValues("NAME").stream().map(String.class::cast).forEach(userNames::add);

        return userNames;
    }

    /**
     * Set the TwitchNotify in our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param webhookId  the ID of the Webhook.
     * @param authToken  the Auth-token to verify the access.
     * @param twitchName the Username of the Twitch User.
     */
    public void addTwitchWebhook(String guildId, String webhookId, String authToken, String twitchName) {

        // Check if there is already a Webhook set.
        removeTwitchWebhook(guildId, twitchName);

        // Add a new entry into the Database.
        saveEntity(new WebhookTwitch(guildId, twitchName, webhookId, authToken));
    }

    /**
     * Remove a Twitch Notifier entry from our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Name of the Twitch User.
     */
    public void removeTwitchWebhook(String guildId, String twitchName) {

        // Check if there is a Webhook set.
        if (isTwitchSetup(guildId, twitchName)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getTwitchWebhook(guildId, twitchName);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM TwitchNotify WHERE GID=? AND NAME=?", guildId, twitchName);
        }
    }

    /**
     * Check if the Twitch Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId) {
        return getEntity(WebhookTwitch.class, "SELECT * FROM TwitchNotify WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if the Twitch Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId, String twitchName) {
        return getEntity(WebhookTwitch.class, "SELECT * FROM TwitchNotify WHERE GID=? AND NAME=?", guildId, twitchName).isSuccess();
    }

    //endregion

    //region Instagram Notifier

    /**
     * Get the InstagramNotify data.
     *
     * @param guildId the ID of the Guild.
     * @param name    the Name of the Instagram User.
     * @return {@link WebhookInstagram} with all the needed data.
     */
    public WebhookInstagram getInstagramWebhook(String guildId, String name) {
        SQLResponse sqlResponse = getEntity(WebhookInstagram.class, "SELECT * FROM InstagramNotify WHERE GID=? AND NAME=?", guildId, name);
        return sqlResponse.isSuccess() ? (WebhookInstagram) sqlResponse.getEntity() : null;
    }

    /**
     * Get the InstagramNotify data.
     *
     * @param name the Name of the Instagram User.
     * @return {@link List<WebhookInstagram>} with all the needed data.
     */
    public List<WebhookInstagram> getInstagramWebhookByName(String name) {
        SQLResponse sqlResponse = getEntity(WebhookInstagram.class, "SELECT * FROM InstagramNotify WHERE NAME=?", name);
        return sqlResponse.isSuccess() ? sqlResponse.getEntities().stream().map(WebhookInstagram.class::cast).toList() : new ArrayList<>();
    }

    /**
     * Get the all Instagram-Notifier.
     *
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllInstagramUsers() {

        ArrayList<String> usernames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the InstagramNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM InstagramNotify").getValues("NAME").stream().map(String.class::cast).forEach(usernames::add);

        return usernames;
    }

    /**
     * Get every Instagram-Notifier that has been set up for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllInstagramUsers(String guildId) {

        ArrayList<String> usernames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the RedditNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM InstagramNotify WHERE GID=?", guildId).getValues("NAME").stream().map(String.class::cast).forEach(usernames::add);

        return usernames;
    }

    /**
     * Set the InstagramNotify in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     * @param name      the Name of the Instagram User.
     */
    public void addInstagramWebhook(String guildId, String webhookId, String authToken, String name) {

        // Check if there is already a Webhook set.
        removeInstagramWebhook(guildId, name);

        // Add a new entry into the Database.
        saveEntity(new WebhookInstagram(guildId, name, webhookId, authToken));
    }

    /**
     * Remove an Instagram Notifier entry from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param name    the Name of the Instagram User.
     */
    public void removeInstagramWebhook(String guildId, String name) {

        // Check if there is a Webhook set.
        if (isInstagramSetup(guildId, name)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getInstagramWebhook(guildId, name);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM InstagramNotify WHERE GID=? AND NAME=?", guildId, name);
        }
    }

    /**
     * Check if the Instagram Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isInstagramSetup(String guildId) {
        return getEntity(WebhookInstagram.class, "SELECT * FROM InstagramNotify WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if the Instagram Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param name    the Name of the Instagram User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isInstagramSetup(String guildId, String name) {
        return getEntity(WebhookInstagram.class, "SELECT * FROM InstagramNotify WHERE GID=? AND NAME=?", guildId, name).isSuccess();
    }

    //endregion

    //region Reddit Notifier

    /**
     * Get the RedditNotify data.
     *
     * @param guildId   the ID of the Guild.
     * @param subreddit the Name of the Subreddit.
     * @return {@link WebhookReddit} with all the needed data.
     */
    public WebhookReddit getRedditWebhook(String guildId, String subreddit) {
        SQLResponse sqlResponse = getEntity(WebhookReddit.class, "SELECT * FROM RedditNotify WHERE GID=? AND SUBREDDIT=?", guildId, subreddit);
        return sqlResponse.isSuccess() ? (WebhookReddit) sqlResponse.getEntity() : null;
    }

    /**
     * Get the RedditNotify data.
     *
     * @param subreddit the Name of the Subreddit.
     * @return {@link List<WebhookReddit>} with all the needed data.
     */
    public List<WebhookReddit> getRedditWebhookBySub(String subreddit) {
        SQLResponse sqlResponse = getEntity(WebhookReddit.class, "SELECT * FROM RedditNotify WHERE SUBREDDIT=?", subreddit);
        return sqlResponse.isSuccess() ? sqlResponse.getEntities().stream().map(WebhookReddit.class::cast).toList() : new ArrayList<>();
    }

    /**
     * Get the all Reddit-Notifier.
     *
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllSubreddits() {

        ArrayList<String> subreddits = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the RedditNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM RedditNotify").getValues("SUBREDDIT").stream().map(String.class::cast).forEach(subreddits::add);

        return subreddits;
    }

    /**
     * Get every Reddit-Notifier that has been set up for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllSubreddits(String guildId) {

        ArrayList<String> subreddits = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the RedditNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM RedditNotify WHERE GID=?", guildId).getValues("SUBREDDIT").stream().map(String.class::cast).forEach(subreddits::add);

        return subreddits;
    }

    /**
     * Set the RedditNotify in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     * @param subreddit the Name of the Subreddit.
     */
    public void addRedditWebhook(String guildId, String webhookId, String authToken, String subreddit) {

        // Check if there is already a Webhook set.
        removeRedditWebhook(guildId, subreddit);

        // Add a new entry into the Database.
        saveEntity(new WebhookReddit(guildId, subreddit, webhookId, authToken));
    }

    /**
     * Remove a Reddit Notifier entry from our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param subreddit the Name of the Subreddit.
     */
    public void removeRedditWebhook(String guildId, String subreddit) {

        // Check if there is a Webhook set.
        if (isRedditSetup(guildId, subreddit)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getRedditWebhook(guildId, subreddit);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM RedditNotify WHERE GID=? AND SUBREDDIT=?", guildId, subreddit);
        }
    }

    /**
     * Check if the Reddit Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isRedditSetup(String guildId) {
        return getEntity(WebhookReddit.class, "SELECT * FROM RedditNotify WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if the Reddit Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId   the ID of the Guild.
     * @param subreddit the Name of the Subreddit.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isRedditSetup(String guildId, String subreddit) {
        return getEntity(WebhookReddit.class, "SELECT * FROM RedditNotify WHERE GID=? AND SUBREDDIT=?", guildId, subreddit).isSuccess();
    }

    //endregion

    //region YouTube Notifier

    /**
     * Get the YouTubeNotify data.
     *
     * @param guildId        the ID of the Guild.
     * @param youtubeChannel the Username of the YouTube channel.
     * @return {@link WebhookYouTube} with all the needed data.
     */
    public WebhookYouTube getYouTubeWebhook(String guildId, String youtubeChannel) {
        SQLResponse sqlResponse = getEntity(WebhookYouTube.class, "SELECT * FROM YouTubeNotify WHERE GID=? AND NAME=?", guildId, youtubeChannel);
        return sqlResponse.isSuccess() ? (WebhookYouTube) sqlResponse.getEntity() : null;
    }

    /**
     * Get the YouTubeNotify data.
     *
     * @param youtubeChannel the Username of the YouTube channel.
     * @return {@link List<WebhookYouTube>} with all the needed data.
     */
    public List<WebhookYouTube> getYouTubeWebhooksByName(String youtubeChannel) {
        SQLResponse sqlResponse = getEntity(WebhookYouTube.class, "SELECT * FROM YouTubeNotify WHERE NAME=?", youtubeChannel);
        return sqlResponse.isSuccess() ? sqlResponse.getEntities().stream().map(WebhookYouTube.class::cast).toList() : new ArrayList<>();
    }

    /**
     * Get the all YouTube-Notifier.
     *
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllYouTubeChannels() {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the YouTubeNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM YouTubeNotify").getValues("NAME").stream().map(String.class::cast).forEach(userNames::add);

        return userNames;
    }

    /**
     * Get every YouTube-Notifier that has been set up for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllYouTubeChannels(String guildId) {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the YouTubeNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM YouTubeNotify WHERE GID=?", guildId).getValues("NAME").stream().map(String.class::cast).forEach(userNames::add);

        return userNames;
    }

    /**
     * Set the YouTubeNotify in our Database.
     *
     * @param guildId        the ID of the Guild.
     * @param webhookId      the ID of the Webhook.
     * @param authToken      the Auth-token to verify the access.
     * @param youtubeChannel the Username of the YouTube channel.
     */
    public void addYouTubeWebhook(String guildId, String webhookId, String authToken, String youtubeChannel) {

        // Check if there is already a Webhook set.
        removeYouTubeWebhook(guildId, youtubeChannel);

        // Add a new entry into the Database.
        saveEntity(new WebhookYouTube(guildId, youtubeChannel, webhookId, authToken));
    }

    /**
     * Remove a YouTube Notifier entry from our Database.
     *
     * @param guildId        the ID of the Guild.
     * @param youtubeChannel the Name of the YouTube channel.
     */
    public void removeYouTubeWebhook(String guildId, String youtubeChannel) {

        // Check if there is a Webhook set.
        if (isYouTubeSetup(guildId, youtubeChannel)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getYouTubeWebhook(guildId, youtubeChannel);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM YouTubeNotify WHERE GID=? AND NAME=?", guildId, youtubeChannel);
        }
    }

    /**
     * Check if the YouTube Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isYouTubeSetup(String guildId) {
        return getEntity(WebhookYouTube.class, "SELECT * FROM YouTubeNotify WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if the YouTube Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId        the ID of the Guild.
     * @param youtubeChannel the Username of the YouTube channel.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isYouTubeSetup(String guildId, String youtubeChannel) {
        return getEntity(WebhookYouTube.class, "SELECT * FROM YouTubeNotify WHERE GID=? AND NAME=?", guildId, youtubeChannel).isSuccess();
    }

    //endregion

    //region Twitter Notifer

    /**
     * Get the Twitter-Notify data.
     *
     * @param guildId     the ID of the Guild.
     * @param twitterName the Username of the Twitter User.
     * @return {@link WebhookTwitter} with all the needed data.
     */
    public WebhookTwitter getTwitterWebhook(String guildId, String twitterName) {
        SQLResponse sqlResponse = getEntity(WebhookTwitter.class, "SELECT * FROM TwitterNotify WHERE GID=? AND NAME=?", guildId, twitterName);
        return sqlResponse.isSuccess() ? (WebhookTwitter) sqlResponse.getEntity() : null;
    }

    /**
     * Get the TwitterNotify data.
     *
     * @param twitterName the Username of the Twitter User.
     * @return {@link List<WebhookTwitter>} with all the needed data.
     */
    public List<WebhookTwitter> getTwitterWebhooksByName(String twitterName) {
        SQLResponse sqlResponse = getEntity(WebhookTwitter.class, "SELECT * FROM TwitterNotify WHERE NAME=?", twitterName);
        return sqlResponse.isSuccess() ? sqlResponse.getEntities().stream().map(WebhookTwitter.class::cast).toList() : new ArrayList<>();
    }

    /**
     * Get the all Twitter-Notifier.
     *
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllTwitterNames() {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the TwitterNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM TwitterNotify").getValues("NAME").stream().map(String.class::cast).forEach(userNames::add);

        return userNames;
    }

    /**
     * Get every Twitter-Notifier that has been set up for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public List<String> getAllTwitterNames(String guildId) {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the TwitterNotify Table by the GuildID.
        sqlConnector.querySQL("SELECT * FROM TwitterNotify WHERE GID=?", guildId).getValues("NAME").stream().map(String.class::cast).forEach(userNames::add);

        return userNames;
    }

    /**
     * Set the TwitterNotify in our Database.
     *
     * @param guildId     the ID of the Guild.
     * @param webhookId   the ID of the Webhook.
     * @param authToken   the Auth-token to verify the access.
     * @param twitterName the Username of the Twitter User.
     */
    public void addTwitterWebhook(String guildId, String webhookId, String authToken, String twitterName) {

        // Check if there is already a Webhook set.
        removeTwitterWebhook(guildId, twitterName);

        // Add a new entry into the Database.
        saveEntity(new WebhookTwitter(guildId, twitterName, webhookId, authToken));
    }

    /**
     * Remove a Twitter Notifier entry from our Database.
     *
     * @param guildId     the ID of the Guild.
     * @param twitterName the Name of the Twitter User.
     */
    public void removeTwitterWebhook(String guildId, String twitterName) {
        // Check if there is a Webhook set.
        if (isTwitterSetup(guildId, twitterName)) {

            // Get the Guild from the ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            if (guild != null) {
                Webhook webhookEntity = getTwitterWebhook(guildId, twitterName);
                // Delete the existing Webhook.
                guild.retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getId().equalsIgnoreCase(webhookEntity.getChannelId()) && webhook.getToken().equalsIgnoreCase(webhookEntity.getToken())).forEach(webhook -> webhook.delete().queue()));
            }

            // Delete the entry.
            sqlConnector.querySQL("DELETE FROM TwitterNotify WHERE GID=? AND NAME=?", guildId, twitterName);
        }
    }

    /**
     * Check if the Twitter Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitterSetup(String guildId) {
        return getEntity(WebhookTwitter.class, "SELECT * FROM TwitterNotify WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if the Twitter Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId     the ID of the Guild.
     * @param twitterName the Username of the Twitter User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitterSetup(String guildId, String twitterName) {
        return getEntity(WebhookTwitter.class, "SELECT * FROM TwitterNotify WHERE GID=? AND NAME=?", guildId, twitterName).isSuccess();
    }

    //endregion

    //endregion

    //region Roles

    //region AutoRoles

    /**
     * Get the all AutoRoles saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<AutoRole>} as List with all Roles.
     */
    public List<AutoRole> getAutoRoles(String guildId) {
        SQLResponse sqlResponse = getEntity(AutoRole.class, "SELECT * FROM AutoRoles WHERE GID=?", guildId);
        return sqlResponse.isSuccess() ? sqlResponse.getEntities().stream().map(AutoRole.class::cast).toList() : new ArrayList<>();
    }

    /**
     * Add a AutoRole in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void addAutoRole(String guildId, String roleId) {
        // Check if there is a role in the database.
        if (!isAutoRoleSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            saveEntity(new AutoRole(guildId, roleId));
        }
    }

    /**
     * Remove a AutoRole from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void removeAutoRole(String guildId, String roleId) {
        // Check if there is a role in the database.
        if (isAutoRoleSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            deleteEntity(new AutoRole(guildId, roleId));
        }
    }

    /**
     * Check if a AutoRole has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isAutoRoleSetup(String guildId) {
        return getEntity(AutoRole.class, "SELECT * FROM AutoRoles WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if a AutoRole has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isAutoRoleSetup(String guildId, String roleId) {
        return getEntity(AutoRole.class, "SELECT * FROM AutoRoles WHERE GID=? AND RID=?", guildId, roleId).isSuccess();
    }

    //endregion

    //region Level Rewards

    //region Chat Rewards

    /**
     * Get the all Chat Rewards saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link HashMap<>} as List with all Role IDs and the needed Level.
     */
    public Map<Integer, String> getChatLevelRewards(String guildId) {

        // Create a new HashMap to save the Role Ids and their needed level.
        Map<Integer, String> rewards = new HashMap<>();

        getEntity(ChatAutoRole.class, "SELECT * FROM ChatLevelAutoRoles WHERE GID=?", guildId).getEntities().stream().map(ChatAutoRole.class::cast).forEach(chatAutoRole -> rewards.put(chatAutoRole.getLevel(), chatAutoRole.getRoleId()));

        // Return the HashMap.
        return rewards;
    }

    /**
     * Add a Chat Level Reward Role in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @param level   the Level required to get this Role.
     */
    public void addChatLevelReward(String guildId, String roleId, int level) {
        // Check if there is a role in the database.
        if (!isChatLevelRewardSetup(guildId, roleId, level)) {
            // Add a new entry into the Database.
            saveEntity(new ChatAutoRole(guildId, roleId, level));
        }
    }

    /**
     * Remove a Chat Level Reward Role from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param level   the Level required to get this Role.
     */
    public void removeChatLevelReward(String guildId, int level) {
        // Check if there is a role in the database.
        if (isChatLevelRewardSetup(guildId)) {
            // Add a new entry into the Database.
            sqlConnector.querySQL("DELETE FROM ChatLevelAutoRoles WHERE GID=? AND LVL=?", guildId, level);
        }
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId) {
        return getEntity(ChatAutoRole.class, "SELECT * FROM ChatLevelAutoRoles WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId, String roleId) {
        return getEntity(ChatAutoRole.class, "SELECT * FROM ChatLevelAutoRoles WHERE GID=? AND RID=?", guildId, roleId).isSuccess();
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @param level   the Level needed to get the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId, String roleId, int level) {
        return getEntity(ChatAutoRole.class, "SELECT * FROM ChatLevelAutoRoles WHERE GID=? AND RID=? AND LVL=?", guildId, roleId, level).isSuccess();
    }

    //endregion

    //region Voice Rewards

    /**
     * Get the all Voice Rewards saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Map<>} as List with all Role IDs and the needed Level.
     */
    public Map<Integer, String> getVoiceLevelRewards(String guildId) {

        // Create a new HashMap to save the Role Ids and their needed level.
        Map<Integer, String> rewards = new HashMap<>();

        getEntity(VoiceAutoRole.class, "SELECT * FROM VoiceLevelAutoRoles WHERE GID=?", guildId).getEntities().stream().map(VoiceAutoRole.class::cast).forEach(voiceAutoRole -> rewards.put(voiceAutoRole.getLevel(), voiceAutoRole.getRoleId()));

        // Return the HashMap.
        return rewards;
    }

    /**
     * Add a Voice Level Reward Role in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @param level   the Level required to get this Role.
     */
    public void addVoiceLevelReward(String guildId, String roleId, int level) {

        // Check if there is a role in the database.
        if (!isVoiceLevelRewardSetup(guildId, roleId, level)) {
            // Add a new entry into the Database.
            saveEntity(new VoiceAutoRole(guildId, roleId, level));
        }
    }

    /**
     * Remove a Voice Level Reward Role from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param level   the Level required to get this Role.
     */
    public void removeVoiceLevelReward(String guildId, int level) {
        // Check if there is a role in the database.
        if (isVoiceLevelRewardSetup(guildId)) {
            // Add a new entry into the Database.
            sqlConnector.querySQL("DELETE FROM VCLevelAutoRoles WHERE GID=? AND LVL=?", guildId, level);
        }
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId) {
        return getEntity(VoiceAutoRole.class, "SELECT * FROM VCLevelAutoRoles WHERE GID=?", guildId).isSuccess();
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId, String roleId) {
        return getEntity(VoiceAutoRole.class, "SELECT * FROM VCLevelAutoRoles WHERE GID=? AND RID=?", guildId, roleId).isSuccess();
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @param level   the Level needed to get the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId, String roleId, int level) {
        return getEntity(VoiceAutoRole.class, "SELECT * FROM VCLevelAutoRoles WHERE GID=? AND RID=? AND LVL=?", guildId, roleId, level).isSuccess();
    }

    //endregion

    //endregion

    //endregion

    //region Invite

    /**
     * Get a List of every saved Invite from our Database.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<String>} as List with {@link InviteContainer}.
     */
    public List<InviteContainer> getInvites(String guildId) {
        ArrayList<InviteContainer> invites = new ArrayList<>();

        SQLResponse sqlResponse = getEntity(Invite.class, "SELECT * FROM Invites WHERE GID=?", guildId);

        if (!sqlResponse.isSuccess()) return invites;

        sqlResponse.getEntities().stream().map(o -> {
            Invite invite = (Invite) o;
            return new InviteContainer(invite.getUserId(), invite.getGuild(), invite.getCode(), invite.getUses(), false);
        }).forEach(invites::add);
        return invites;
    }

    /**
     * Check if the given Invite Data is saved in our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite.
     * @return {@link Boolean} as Result if true, then it's saved in our Database | if false, we couldn't find anything.
     */
    public boolean existsInvite(String guildId, String inviteCreator, String inviteCode) {
        return getEntity(Invite.class, "SELECT * FROM Invites WHERE GID=? AND UID=? AND CODE=?", guildId, inviteCreator, inviteCode).isSuccess();
    }

    /**
     * Remove an entry from our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param inviteCode the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCode) {
        sqlConnector.querySQL("DELETE FROM Invites WHERE GID=? AND CODE=?", guildId, inviteCode);
    }

    /**
     * Change the data of a saved Invite or create a new entry in our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite Code.
     * @param inviteUsage   the Usage count of the Invite.
     */
    public void setInvite(String guildId, String inviteCreator, String inviteCode, long inviteUsage) {
        // Check if there is an entry with the same data.
        if (existsInvite(guildId, inviteCreator, inviteCode)) {
            // Update entry.
            updateEntity(getInvite(guildId, inviteCode), new Invite(guildId, inviteCreator, inviteUsage, inviteCode), true);
        } else {
            saveEntity(new Invite(guildId, inviteCreator, inviteUsage, inviteCode));
        }
    }

    /**
     * Get the Invite from our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param inviteCode the Code of the Invite.
     * @return {@link Invite} as result if true, then it's saved in our Database | may be null.
     */
    public Invite getInvite(String guildId, String inviteCode) {
        SQLResponse sqlResponse = getEntity(Invite.class, "SELECT * FROM Invites WHERE GID=? AND CODE=?", guildId, inviteCode);
        return sqlResponse.isSuccess() ? (Invite) sqlResponse.getEntity() : null;
    }

    /**
     * Get the Invite from our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite.
     * @return {@link Invite} as result if true, then it's saved in our Database | may be null.
     */
    public Invite getInvite(String guildId, String inviteCreator, String inviteCode) {
        SQLResponse sqlResponse = getEntity(Invite.class, "SELECT * FROM Invites WHERE GID=? AND UID=? AND CODE=?", guildId, inviteCreator, inviteCode);
        return sqlResponse.isSuccess() ? (Invite) sqlResponse.getEntity() : null;
    }

    /**
     * Remove an entry from our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCreator, String inviteCode) {
        deleteEntity(getInvite(guildId, inviteCreator, inviteCode));
    }

    /**
     * Remove an entry from our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCreator, String inviteCode, int inviteUsage) {
        deleteEntity(new Invite(guildId, inviteCreator, inviteUsage, inviteCode));
    }

    /**
     * Remove all entries from our Database.
     *
     * @param guildId the ID of the Guild.
     */
    public void clearInvites(String guildId) {
        sqlConnector.querySQL("DELETE FROM Invites WHERE GID=?", guildId);
    }

    //endregion

    //region Configuration

    //region Join Message.

    /**
     * Get the Join Message of the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return the Message as {@link String}
     */
    public String getMessage(String guildId) {
        SQLResponse sqlResponse = getEntity(Setting.class, "SELECT * FROM Settings WHERE GID=? AND NAME=?", guildId, "message_join");
        return sqlResponse.isSuccess() ? ((Setting) sqlResponse.getEntity()).getStringValue() : "Welcome %user_mention%!\nWe wish you a great stay on %guild_name%";
    }

    /**
     * Change the current Join Message of a Guild.
     *
     * @param guildId the ID of the Guild.
     * @param content the Join Message.
     */
    public void setMessage(String guildId, String content) {

        if (isMessageSetup(guildId)) {
            // If there is already an entry just replace it.
            updateEntity(getSetting(guildId, "message_join"), new Setting(guildId, "message_join", content), true);
        } else {
            // Create a new entry, if there was none.
            saveEntity(new Setting(guildId, "message_join", content));
        }
    }

    /**
     * Check if there is a custom Join Message set in our Database.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result. If true, then there is an entry in our Database | If false, there is no entry in our Database for that Guild.
     */
    public boolean isMessageSetup(String guildId) {
        return getEntity(Setting.class, "SELECT * FROM Settings WHERE GID=? AND NAME=?", guildId, "message_join").isSuccess();
    }

    //endregion

    //region Chat Protector / Word Blacklist

    /**
     * Get every Blacklisted Word saved in our Database from the Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<String>} as list with every Blacklisted Word.
     */
    public List<String> getChatProtectorWords(String guildId) {

        if (isChatProtectorSetup(guildId)) {
            return getEntity(Blacklist.class, "SELECT * FROM ChatProtector WHERE GID = ?", guildId).getEntities().stream().map(String::valueOf).toList();
        }

        // return the ArrayList with every blacklisted Word. (Can be empty!)
        return new ArrayList<>();
    }

    /**
     * Check if there is an entry in our Database for the wanted Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result. If true, there is an entry in our Database | If false, there is no entry in our Database.
     */
    public boolean isChatProtectorSetup(String guildId) {
        return getEntity(Blacklist.class, "SELECT * FROM ChatProtector WHERE GID = ?", guildId).isSuccess();
    }

    /**
     * Check if there is an entry in our Database for the wanted Guild.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word that should be checked.
     * @return {@link Boolean} as result. If true, there is an entry in our Database | If false, there is no entry in our Database.
     */
    public boolean isChatProtectorSetup(String guildId, String word) {
        return getEntity(Blacklist.class, "SELECT * FROM ChatProtector WHERE GID = ? AND WORD = ?", guildId, word).isSuccess();
    }

    /**
     * Add a new Word to the blacklist for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word to be blocked.
     */
    public void addChatProtectorWord(String guildId, String word) {

        // Check if there is already an entry for it.
        if (isChatProtectorSetup(guildId, word)) return;

        // If not then just add it.
        saveEntity(new Blacklist(guildId, word));
    }

    /**
     * Remove a Word from the blacklist for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word to be removed.
     */
    public void removeChatProtectorWord(String guildId, String word) {
        // Check if there is no entry for it.
        if (!isChatProtectorSetup(guildId, word)) return;

        // If so then delete it.
        deleteEntity(new Blacklist(guildId, word));
    }

    //endregion

    //region Settings

    /**
     * Get the current Setting by the Guild and its Identifier.
     *
     * @param guildId     the ID of the Guild.
     * @param settingName the Identifier of the Setting.
     * @return {@link Setting} which stores every information needed.
     */
    public Setting getSetting(String guildId, String settingName) {
        // Check if there is an entry in the database.
        if (hasSetting(guildId, settingName)) {
            return getEntity(new Setting(), "SELECT * FROM Settings WHERE GID = :gid AND NAME = :name", Map.of("gid", guildId, "name", settingName));
        } else {
            // Check if everything is alright with the config.
            checkSetting(guildId, settingName);
        }

        return new Setting(guildId, settingName, true);
    }

    /**
     * Get the every Setting by the Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List<Setting>} which is a List with every Setting that stores every information needed.
     */
    public List<Setting> getAllSettings(String guildId) {
        return getEntityList(new Setting(), "SELECT * FROM Settings WHERE GID = :gid", Map.of("gid", guildId));
    }


    /**
     * Set the Setting by its Identifier.
     *
     * @param setting the Setting.
     */
    public void setSetting(Setting setting) {
        setSetting(setting.getGuild(), setting.getName(), setting.getStringValue());
    }

    /**
     * Set the Setting by the Guild and its Identifier.
     *
     * @param guildId      the ID of the Guild.
     * @param settingName  the Identifier of the Setting.
     * @param settingValue the Value of the Setting.
     */
    public void setSetting(String guildId, String settingName, Object settingValue) {

        // Check if it is null.
        if (settingValue == null) createSettings(guildId);

        // Check if there is an entry.
        if (hasSetting(guildId, settingName)) {
            // If so update it.
            updateEntity(new Setting(guildId, settingName, settingValue));
        } else {
            saveEntity(new Setting(guildId, settingName, settingValue));
        }
    }

    /**
     * Check if there is a Setting entry for the Guild.
     *
     * @param guildId the ID of the Guild.
     * @param setting the Setting itself.
     * @return {@link Boolean} as result. If true, there is a Setting Entry for the Guild | if false, there is no Entry for it.
     */
    public boolean hasSetting(String guildId, Setting setting) {
        return hasSetting(guildId, setting.getName());
    }

    /**
     * Check if there is a Setting entry for the Guild.
     *
     * @param guildId     the ID of the Guild.
     * @param settingName the Identifier of the Setting.
     * @return {@link Boolean} as result. If true, there is a Setting Entry for the Guild | if false, there is no Entry for it.
     */
    public boolean hasSetting(String guildId, String settingName) {
        return getEntity(new Setting(), "SELECT * FROM Settings WHERE GID = :gid AND NAME = :name", Map.of("gid", guildId, "name", settingName)) != null;
    }

    /**
     * Check if there is an entry for the Setting, if not create one for every Setting that doesn't have an entry.
     *
     * @param guildId the ID of the Guild.
     * @param setting the Setting itself.
     */
    public void checkSetting(String guildId, Setting setting) {
        checkSetting(guildId, setting.getName());
    }

    /**
     * Check if there is an entry for the Setting, if not create one for every Setting that doesn't have an entry.
     *
     * @param guildId     the ID of the Guild.
     * @param settingName the Identifier of the Setting.
     */
    public void checkSetting(String guildId, String settingName) {
        // Check if the Setting exists in our Database.
        if (!hasSetting(guildId, settingName)) {
            // If not then creat every Setting that doesn't exist for the Guild.
            createSettings(guildId);
        }
    }

    /**
     * Create Settings entries for the Guild
     *
     * @param guildId the ID of the Guild.
     */
    public void createSettings(String guildId) {
        // Create the Chat Prefix Setting.
        if (!hasSetting(guildId, "chatprefix")) setSetting(new Setting(guildId, "chatprefix", "ree!"));

        // Create the Level Message Setting.
        if (!hasSetting(guildId, "level_message")) setSetting(new Setting(guildId, "level_message", false));

        // Create the Language Setting.
        if (!hasSetting(guildId, "configuration_language")) setSetting(new Setting(guildId, "configuration_language", "en_GB"));

        // Create the Join Message Setting
        if (!hasSetting(guildId, "message_join"))
            setSetting(new Setting(guildId, "message_join", "Welcome %user_mention%!\nWe wish you a great stay on %guild_name%"));

        // Create Command Settings.
        for (ICommand command : Main.getInstance().getCommandManager().getCommands()) {

            // Skip the hidden Commands.
            if (command.getClass().getAnnotation(Command.class).category() == Category.HIDDEN) continue;

            if (!hasSetting(guildId, "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase()))
                setSetting(guildId, "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase(), true);
        }

        // Create Log Settings.
        if (!hasSetting(guildId, "logging_invite")) setSetting(guildId, "logging_invite", true);
        if (!hasSetting(guildId, "logging_memberjoin")) setSetting(guildId, "logging_memberjoin", true);
        if (!hasSetting(guildId, "logging_memberleave")) setSetting(guildId, "logging_memberleave", true);
        if (!hasSetting(guildId, "logging_memberban")) setSetting(guildId, "logging_memberban", true);
        if (!hasSetting(guildId, "logging_memberunban")) setSetting(guildId, "logging_memberunban", true);
        if (!hasSetting(guildId, "logging_nickname")) setSetting(guildId, "logging_nickname", true);
        if (!hasSetting(guildId, "logging_voicejoin")) setSetting(guildId, "logging_voicejoin", true);
        if (!hasSetting(guildId, "logging_voicemove")) setSetting(guildId, "logging_voicemove", true);
        if (!hasSetting(guildId, "logging_voiceleave")) setSetting(guildId, "logging_voiceleave", true);
        if (!hasSetting(guildId, "logging_roleadd")) setSetting(guildId, "logging_roleadd", true);
        if (!hasSetting(guildId, "logging_roleremove")) setSetting(guildId, "logging_roleremove", true);
        if (!hasSetting(guildId, "logging_voicechannel")) setSetting(guildId, "logging_voicechannel", true);
        if (!hasSetting(guildId, "logging_textchannel")) setSetting(guildId, "logging_textchannel", true);
        if (!hasSetting(guildId, "logging_rolecreate")) setSetting(guildId, "logging_rolecreate", true);
        if (!hasSetting(guildId, "logging_roledelete")) setSetting(guildId, "logging_roledelete", true);
        if (!hasSetting(guildId, "logging_rolename")) setSetting(guildId, "logging_rolename", true);
        if (!hasSetting(guildId, "logging_rolemention")) setSetting(guildId, "logging_rolemention", true);
        if (!hasSetting(guildId, "logging_rolehoisted")) setSetting(guildId, "logging_rolehoisted", true);
        if (!hasSetting(guildId, "logging_rolepermission")) setSetting(guildId, "logging_rolepermission", true);
        if (!hasSetting(guildId, "logging_rolecolor")) setSetting(guildId, "logging_rolecolor", true);
        if (!hasSetting(guildId, "logging_messagedelete")) setSetting(guildId, "logging_messagedelete", true);
        if (!hasSetting(guildId, "logging_timeout")) setSetting(guildId, "logging_timeout", true);
    }

    //endregion

    //endregion

    //region Stats

    /**
     * Retrieve the Statistics of this day.
     *
     * @return the Statistics.
     */
    public Statistics getStatisticsOfToday() {
        LocalDate today = LocalDate.now();
        return getStatistics(today.getDayOfMonth(), today.getMonthValue(), today.getYear());
    }

    /**
     * Retrieve the Statistics of this day.
     *
     * @param day the day the statics has been taken from.
     * @param month the month the statics has been taken from.
     * @param year the year the statics has been taken from.
     * @return the Statistics.
     */
    public Statistics getStatistics(int day, int month, int year) {
        return getEntity(new Statistics(), "SELECT * FROM Statistics WHERE DAY = :day AND MONTH = :month AND YEAR = :year", Map.of("day", day, "month", month, "year", year));
    }

    /**
     * Retrieve the Statistics of a month.
     *
     * @param month the month you want to receive the Statistics from.
     * @return all {@link Statistics} of the given month.
     */
    public List<Statistics> getStatisticsOfMonth(int month) {
        return getEntityList(new Statistics(), "SELECT * FROM Statistics WHERE MONTH = :month", Map.of("month", month));
    }

    /**
     * Update or add new/existing Statistics.
     *
     * @param statisticObject the {@link JsonObject} for the statistic.
     */
    public void updateStatistic(JsonObject statisticObject) {
        LocalDate today = LocalDate.now();
        Statistics statistics = getEntity(new Statistics(), "SELECT * FROM Statistics WHERE DAY = :day AND MONTH = :month AND YEAR = :year", Map.of("day", today.getDayOfMonth(), "month", today.getMonthValue(), "year", today.getYear()));
        if (statistics != null) {
            statistics.setStatsObject(statisticObject);
            updateEntity(statistics);
        } else {
            statistics = new Statistics(today.getDayOfMonth(), today.getMonthValue(), today.getYear(), statisticObject);
            saveEntity(statistics);
        }
    }

    /**
     * Get the Stats of the Command.
     *
     * @param command the Command.
     * @return the Stats of the Command.
     */
    public CommandStats getStatsCommandGlobal(String command) {
        return getEntity(new CommandStats(), "SELECT * FROM CommandStats WHERE COMMAND = :command", Map.of("command", command));
    }

    /**
     * Get the Stats of the Command in the specific Guild.
     *
     * @param guildId the ID of the Guild.
     * @param command the Command.
     * @return the Stats of the Command.
     */
    public GuildCommandStats getStatsCommand(String guildId, String command) {
        return getEntity(new GuildCommandStats(), "SELECT * FROM GuildStats WHERE GID = :gid AND COMMAND = :command", Map.of("gid", guildId, "command", command));
    }

    /**
     * Get all the Command-Stats related to the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return all the Command-Stats related to the given Guild.
     */
    public List<GuildCommandStats> getStats(String guildId) {
        return getEntityList(new GuildCommandStats(), "SELECT * FROM GuildStats WHERE GID=:gid ORDER BY CAST(uses as UNSIGNED) DESC LIMIT 5", Map.of("gid", guildId));
    }

    /**
     * Get all the Command-Stats globally.
     *
     * @return all the Command-Stats globally.
     */
    public List<CommandStats> getStatsGlobal() {
        return getEntityList(new CommandStats(), "SELECT * FROM CommandStats ORDER BY CAST(uses as UNSIGNED) DESC LIMIT 5", null);
    }

    /**
     * Check if there is any saved Stats for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isStatsSaved(String guildId) {
        return getEntity(new GuildCommandStats(), "SELECT * FROM GuildStats WHERE GID = :gid ", Map.of("gid", guildId)) != null;
    }

    /**
     * Check if there is any saved Stats for the given Guild and Command.
     *
     * @param guildId the ID of the Guild.
     * @param command the Name of the Command.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isStatsSaved(String guildId, String command) {
        return getEntity(new GuildCommandStats(), "SELECT * FROM GuildStats WHERE GID = :gid AND COMMAND = :command", Map.of("gid", guildId, "command", command)) != null;
    }

    /**
     * Check if there is any saved Stats for the given Command.
     *
     * @param command the Name of the Command.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isStatsSavedGlobal(String command) {
        return getEntity(new CommandStats(), "SELECT * FROM CommandStats WHERE COMMAND = :command", Map.of("command", command)) != null;
    }

    /**
     * Save the Stats of the Command in the Database.
     *
     * @param guildId the ID of the Guild.
     * @param command the Command.
     */
    public void addStats(String guildId, String command) {
        Statistics statistics = getStatisticsOfToday();
        JsonObject jsonObject = statistics != null ? statistics.getStatsObject() : new JsonObject();
        JsonObject commandStats = statistics != null && jsonObject.has("command") ? jsonObject.getAsJsonObject("command") : new JsonObject();

        if (commandStats.has(command) && commandStats.get(command).isJsonPrimitive()) {
            commandStats.addProperty(command, commandStats.getAsJsonPrimitive(command).getAsInt() + 1);
        } else {
            commandStats.addProperty(command, 1);
        }

        jsonObject.add("command", commandStats);

        sqlConnector.getSqlWorker().updateStatistic(jsonObject);

        // Check if there is an entry.
        if (isStatsSaved(guildId, command)) {
            GuildCommandStats newGuildStats = getStatsCommand(guildId, command);
            newGuildStats.setUses(newGuildStats.getUses() + 1);
            updateEntity(newGuildStats);
        } else {
            saveEntity(new GuildCommandStats(guildId, command, 1));
        }

        // Check if there is an entry.
        if (isStatsSavedGlobal(command)) {
            CommandStats stats = getStatsCommandGlobal(command);
            stats.setUses(stats.getUses() + 1);
            updateEntity(stats);
        } else {
            saveEntity(new CommandStats(command, 1));
        }
    }

    //endregion

    //region Opt-out

    /**
     * Check if the given User is opted out.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} as result. If true, the User is opted out | If false, the User is not opted out.
     */
    public boolean isOptOut(String guildId, String userId) {
        // Creating a SQL Statement to check if there is an entry in the Opt-out Table by the Guild Id and User Id
        return sqlConnector.querySQL("SELECT * FROM Opt_out WHERE GID=? AND UID=?", guildId, userId).hasResults();
    }

    /**
     * Opt a User out of the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     */
    public void optOut(String guildId, String userId) {
        if (!isOptOut(guildId, userId)) {
            sqlConnector.querySQL("INSERT INTO Opt_out (GID, UID) VALUES (?, ?)", guildId, userId);
        }
    }

    /**
     * Opt in a User to the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     */
    public void optIn(String guildId, String userId) {
        // TODO:: make Opt-In a class Entity.
        if (isOptOut(guildId, userId)) {
            sqlConnector.querySQL(null,"DELETE FROM Opt_out WHERE GID=:gid AND UID=:uid", Map.of("gid", guildId, "uid", userId));
        }
    }

    //endregion

    //region Birthday

    /**
     * Store the birthday of the user in the database
     *
     * @param guildId   the ID of the Guild.
     * @param channelId the ID of the Channel.
     * @param userId    the ID of the User.
     * @param birthday  the birthday of the user.
     */
    public void addBirthday(String guildId, String channelId, String userId, String birthday) {
        try {
            BirthdayWish newBirthday = new BirthdayWish(guildId, channelId, userId, new SimpleDateFormat("dd.MM.yyyy").parse(birthday));
            if (isBirthdaySaved(guildId, userId)) {
                updateEntity(newBirthday);
            } else {
                saveEntity(newBirthday);
            }
        } catch (ParseException ignore) {
        }
    }

    /**
     * Check if there is any saved birthday for the given User.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     */
    public void removeBirthday(String guildId, String userId) {
        if (isBirthdaySaved(guildId, userId)) {
            sqlConnector.querySQL(new BirthdayWish(), "DELETE FROM BirthdayWish WHERE GID=:gid AND UID=:uid", Map.of("gid", guildId, "uid", userId));
        }
    }

    /**
     * Check if a birthday is saved for the given User.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isBirthdaySaved(String guildId, String userId) {
        return getEntity(new BirthdayWish(),"SELECT * FROM BirthdayWish WHERE GID=:gid AND UID=:uid", Map.of("gid", guildId, "uid", userId)) != null;
    }

    /**
     * Get the birthday of the given User.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link BirthdayWish} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public BirthdayWish getBirthday(String guildId, String userId) {
        return getEntity(new BirthdayWish(),"SELECT * FROM BirthdayWish WHERE GID=:gid AND UID=:uid", Map.of("gid", guildId, "uid", userId));
    }

    /**
     * Get all saved birthdays.
     *
     * @param guildId the ID of the Guild.
     * @return {@link List} of {@link BirthdayWish} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public List<BirthdayWish> getBirthdays(String guildId) {
        return getEntityList(new BirthdayWish(), "SELECT * FROM BirthdayWish WHERE GID=:gid", Map.of("gid", guildId));
    }

    /**
     * Get all saved birthdays.
     *
     * @return {@link List} of {@link BirthdayWish} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public List<BirthdayWish> getBirthdays() {
        return getEntityList(new BirthdayWish(), "SELECT * FROM BirthdayWish", null);
    }

    //endregion

    //region Data delete

    /**
     * Delete Data saved in our Database by the given Guild ID.
     *
     * @param guildId the ID of the Guild.
     */
    public void deleteAllData(String guildId) {
        // TODO:: find a way todo this with Hibernate.
    }

    //endregion

    //region Entity-System

    /**
     * Save an Entity to the Database.
     *
     * @param entity the Entity to save.
     */
    public void saveEntity(Object entity) {
        if (!sqlConnector.isConnected()) {
            if (sqlConnector.connectedOnce()) {
                sqlConnector.connectToSQLServer();
                saveEntity(entity);
            }
        }

        try (SessionFactory sessionFactory = SQLSession.buildSessionFactory()) {
            Session session = sessionFactory.getCurrentSession();

            session.beginTransaction();

            session.persist(entity);

            session.getTransaction().commit();
        }
    }

    /**
     * Update an Entity in the Database.
     *
     * @param r       the Entity.
     */
    public <R> R updateEntity(R r) {
        if (!sqlConnector.isConnected()) {
            if (sqlConnector.connectedOnce()) {
                sqlConnector.connectToSQLServer();
                return updateEntity(r);
            }
        }

        try (SessionFactory sessionFactory = SQLSession.buildSessionFactory()) {
            Session session = sessionFactory.getCurrentSession();

            session.beginTransaction();

            R newEntity = session.merge(r);

            session.getTransaction().commit();

            return newEntity;
        }
    }

    /**
     * Delete an entity from the database
     *
     * @param r the Entity-class instance that is to be deleted.
     */
    public <R> void deleteEntity(R r) {
        if (!sqlConnector.isConnected()) {
            if (sqlConnector.connectedOnce()) {
                sqlConnector.connectToSQLServer();
                deleteEntity(r);
            }
        }

        try (SessionFactory sessionFactory = SQLSession.buildSessionFactory()) {
            Session session = sessionFactory.getCurrentSession();

            session.beginTransaction();

            session.remove(r);

            session.getTransaction().commit();
        }
    }

    /**
     * Constructs a new mapped Version of the Entity-class.
     *
     * @param r The entity to get.
     * @return The mapped entity.
     */
    public <R> List<R> getEntityList(R r, String query, Map<String, Object> parameters) {
        if (query.isEmpty()) {
            return sqlConnector.querySQL(r, "SELECT * FROM " + r.getClass().getSimpleName(), null).getResultList();
        } else {
            return sqlConnector.querySQL(r, "SELECT * FROM " + r.getClass().getSimpleName(), parameters).getResultList();
        }
    }

    /**
     * Constructs a query for the given Class-Entity, and returns a mapped Version of the given Class-Entity.
     *
     * @param r The Class-Entity to get.
     * @param query  The query to use.
     * @param parameters   The arguments to use.
     * @return The mapped Version of the given Class-Entity.
     */
    public <R> R getEntity(R r, String query, Map<String, Object> parameters) {
        List<R> list;

        if (query.isEmpty()) {
            list = sqlConnector.querySQL(r, "SELECT * FROM " + r.getClass().getSimpleName(), null).getResultList();
        } else {
            list = sqlConnector.querySQL(r, "SELECT * FROM " + r.getClass().getSimpleName(), parameters).getResultList();
        }

        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    //endregion
}
