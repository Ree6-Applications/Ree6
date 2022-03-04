package de.presti.ree6.sql;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.UserLevel;
import de.presti.ree6.utils.Setting;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * A Class to actually handle the SQL data.
 * Used to provide Data from the Database and to save Data into the Database.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve", "unused", "SingleStatementInBlock"})
public class SQLWorker {

    // Instance of the SQL Connector to actually access the SQL Database.
    private final SQLConnector sqlConnector;

    /**
     * Constructor to create a new Instance of the SQLWorker with a ref to the SQL-Connector.
     *
     * @param sqlConnector an Instance of the SQL-Connector to retrieve the data from.
     */
    public SQLWorker(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    //region Level

    //region Chat

    /**
     * Get the Chat XP Count of the give UserID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Long} as XP Count.
     */
    public UserLevel getChatLevelData(String guildId, String userId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                userId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return null;
        }

        // Creating a SQL Statement to get the User from the Level Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return the UserLevel data if found.
            if (rs != null && rs.next()) {
                return new UserLevel(userId, getAllChatLevelSorted(guildId).indexOf(userId) + 1, Long.parseLong(rs.getString("XP")));
            }
        } catch (Exception ignore) {}

        // Return a new UserLEve if there was an error OR if the user isn't in the database.
        return new UserLevel(userId, 0, 0);
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInChatLevel(String guildId, String userId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                userId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the User from the Level Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Give the wanted User more XP.
     *
     * @param guildId   the ID of the Guild.
     * @param userLevel the UserLevel Entity with all the information.
     */
    public void addChatLevelData(String guildId, UserLevel userLevel) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }
        // Check if the User is already saved in the Database.
        if (existsInChatLevel(guildId, userLevel.getUserId())) {

            // If so change the current XP to the new.
            querySQL("UPDATE Level SET XP='" + userLevel.getExperience() + "' WHERE GID='" + guildId + "' AND UID='" + userLevel.getUserId() + "'");
        } else {

            // If not create a new entry and add the data.
            querySQL("INSERT INTO Level (GID, UID, XP) VALUES ('" + guildId + "', '" + userLevel.getUserId() + "', '" + userLevel.getExperience() + "');");
        }
    }

    /**
     * Get the Top list of the Guild Chat XP.
     *
     * @param guildId the ID of the Guild.
     * @param limit   the Limit of how many should be given back.
     * @return {@link ArrayList<UserLevel>} as container of the User IDs.
     */
    public ArrayList<UserLevel> getTopChat(String guildId, int limit) {

        // Create the List.
        ArrayList<UserLevel> userLevels = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return userLevels;
        }

        // Creating a SQL Statement to get the Entries from the Level Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + limit).executeQuery()) {

            // While there are still entries it should add them to the list.
            while (rs != null && rs.next()) {
                userLevels.add(getChatLevelData(guildId, rs.getString("UID")));
            }
        } catch (Exception ignore) {
        }

        // Return the list.
        return userLevels;
    }

    /**
     * Get the Top list of the Guild Chat XP.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as container of the User IDs.
     */
    public ArrayList<String> getAllChatLevelSorted(String guildId) {

        // Create the List.
        ArrayList<String> userIds = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return userIds;
        }

        // Creating a SQL Statement to get the Entries from the Level Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' ORDER BY cast(xp as unsigned) DESC").executeQuery()) {

            // While there are still entries it should add them to the list.
            while (rs != null && rs.next()) {
                userIds.add(rs.getString("UID"));
            }
        } catch (Exception ignore) {
        }

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
     * @return {@link UserLevel} with information about the User Level.
     */
    public UserLevel getVoiceLevelData(String guildId, String userId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                userId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return null;
        }

        // Creating a SQL Statement to get the User from the VCLevel Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return the UserLevel Data if found.
            if (rs != null && rs.next()) {
                return new UserLevel(userId, getAllVoiceLevelSorted(guildId).indexOf(userId) + 1, Long.parseLong(rs.getString("XP")));
            }
        } catch (Exception ignore) {
        }

        // Return 0 if there was an error OR if the user isn't in the database.
        return new UserLevel(userId, 0, 0);
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInVoiceLevel(String guildId, String userId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                userId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the User from the VCLevel Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Give the wanted User more XP.
     *
     * @param guildId   the ID of the Guild.
     * @param userLevel the UserLevel Entity with all the information.
     */
    public void addVoiceLevelData(String guildId, UserLevel userLevel) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if the User is already saved in the Database.
        if (existsInVoiceLevel(guildId, userLevel.getUserId())) {

            // If so change the current XP to the new.
            querySQL("UPDATE VCLevel SET XP='" + userLevel.getExperience() + "' WHERE GID='" + guildId + "' AND UID='" + userLevel.getUserId() + "'");
        } else {

            // If not create a new entry and add the data.
            querySQL("INSERT INTO VCLevel (GID, UID, XP) VALUES ('" + guildId + "', '" + userLevel.getUserId() + "', '" + userLevel.getExperience() + "');");
        }
    }

    /**
     * Get the Top list of the Guild Voice XP.
     *
     * @param guildId the ID of the Guild.
     * @param limit   the Limit of how many should be given back.
     * @return {@link ArrayList<UserLevel>} as container of the User IDs.
     */
    public ArrayList<UserLevel> getTopVoice(String guildId, int limit) {

        // Create the List.
        ArrayList<UserLevel> userLevels = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return userLevels;
        }

        // Creating a SQL Statement to get the Entries from the VCLevel Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + limit).executeQuery()) {

            // While there are still entries it should add them to the list.
            while (rs != null && rs.next()) {
                userLevels.add(getVoiceLevelData(guildId, rs.getString("UID")));
            }
        } catch (Exception ignore) {
        }

        // Return the list.
        return userLevels;
    }

    /**
     * Get the Top list of the Guild Voice XP.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as container of the User IDs.
     */
    public ArrayList<String> getAllVoiceLevelSorted(String guildId) {

        // Create the List.
        ArrayList<String> userIds = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return userIds;
        }

        // Creating a SQL Statement to get the Entries from the Level Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' ORDER BY cast(xp as unsigned) DESC").executeQuery()) {

            // While there are still entries it should add them to the list.
            while (rs != null && rs.next()) {
                userIds.add(rs.getString("UID"));
            }
        } catch (Exception ignore) {
        }

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
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getLogWebhook(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return new String[]{"0", "No setup!"};
        }

        if (isLogSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[]{"0", "No setup!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "No setup!"};
    }

    /**
     * Set the LogWebhook in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setLogWebhook(String guildId, String webhookId, String authToken) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                webhookId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is already a Webhook set.
        if (isLogSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM LogWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO LogWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the Log Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isLogSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Log Webhook data is in our Database.
     *
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean existsLogData(long webhookId, String authToken) {

        // Check for SQL Statements to prevent SQL Injections.
        if (authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the WebhookID and its Auth-Token.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE CID='" + webhookId + "' AND TOKEN='" + authToken + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Set the LogWebhook in our Database.
     *
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     */
    public void deleteLogWebhook(long webhookId, String authToken) {

        // Check for SQL Statements to prevent SQL Injections.
        if (authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a Webhook with this data.
        if (existsLogData(webhookId, authToken)) {

            // Delete if so.
            querySQL("DELETE FROM LogWebhooks WHERE CID='" + webhookId + "' AND TOKEN='" + authToken + "'");
        }

    }

    //endregion

    //region Welcome

    /**
     * Get the WelcomeWebhooks data.
     *
     * @param guildId the ID of the Guild.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getWelcomeWebhook(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return new String[]{"0", "No setup!"};
        }

        if (isWelcomeSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[]{"0", "No setup!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "No setup!"};
    }

    /**
     * Set the WelcomeWebhooks in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setWelcomeWebhook(String guildId, String webhookId, String authToken) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                webhookId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is already a Webhook set.
        if (isWelcomeSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getWelcomeWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getWelcomeWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM WelcomeWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO WelcomeWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the Welcome Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isWelcomeSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region News

    /**
     * Get the NewsWebhooks data.
     *
     * @param guildId the ID of the Guild.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getNewsWebhook(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return new String[]{"0", "No setup!"};
        }

        if (isNewsSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the NewsWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[]{"0", "No setup!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "No setup!"};
    }

    /**
     * Set the NewsWebhooks in our Database.
     *
     * @param guildId   the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setNewsWebhook(String guildId, String webhookId, String authToken) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                webhookId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is already a Webhook set.
        if (isNewsSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getNewsWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getNewsWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM NewsWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO NewsWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the News Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isNewsSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the NewsWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region Twitch Notifier

    /**
     * Get the TwitchNotify data.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getTwitchWebhook(String guildId, String twitchName) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitchName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return new String[]{"0", "No setup!"};
        }

        if (isTwitchSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[]{"0", "No setup!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "No setup!"};
    }

    /**
     * Get the TwitchNotify data.
     *
     * @param twitchName the Username of the Twitch User.
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String[]> getTwitchWebhooksByName(String twitchName) {

        ArrayList<String[]> webhooks = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (twitchName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return webhooks;
        }

        // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE NAME='" + twitchName + "'").executeQuery()) {

            // Return if there was a match.
            while (rs != null && rs.next()) {
                if (!rs.getString("CID").isEmpty() && !rs.getString("TOKEN").isEmpty())
                    webhooks.add(new String[]{rs.getString("CID"), rs.getString("TOKEN")});
            }
        } catch (Exception ignore) {
        }

        return webhooks;
    }

    /**
     * Get the all Twitch-Notifier.
     *
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String> getAllTwitchNames() {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the TwitchNotify Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify").executeQuery()) {

            // Return if there was a match.
            while (rs != null && rs.next()) {
                userNames.add(rs.getString("NAME"));
            }
        } catch (Exception ignore) {
        }

        return userNames;
    }

    /**
     * Get every Twitch-Notifier that has been setup for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String> getAllTwitchNames(String guildId) {

        ArrayList<String> userNames = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return userNames;
        }

        // Creating a SQL Statement to get the Entry from the TwitchNotify Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            while (rs != null && rs.next()) {
                userNames.add(rs.getString("NAME"));
            }
        } catch (Exception ignore) {
        }

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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                webhookId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitchName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is already a Webhook set.
        if (isTwitchSetup(guildId, twitchName)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getTwitchWebhook(guildId, twitchName)[0]) && webhook.getToken().equalsIgnoreCase(getTwitchWebhook(guildId, twitchName)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO TwitchNotify (GID, NAME, CID, TOKEN) VALUES ('" + guildId + "', '" + twitchName + "', '" + webhookId + "', '" + authToken + "');");
    }

    /**
     * Remove a Twitch Notifier entry from our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Name of the Twitch User.
     */
    public void removeTwitchWebhook(String guildId, String twitchName) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitchName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a Webhook set.
        if (isTwitchSetup(guildId, twitchName)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getTwitchWebhook(guildId, twitchName)[0]) && webhook.getToken().equalsIgnoreCase(getTwitchWebhook(guildId, twitchName)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'");
        }
    }

    /**
     * Check if the Twitch Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Twitch Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId, String twitchName) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitchName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region Twitter Notifer

    /**
     * Get the Twitter-Notify data.
     *
     * @param guildId     the ID of the Guild.
     * @param twitterName the Username of the Twitter User.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getTwitterWebhook(String guildId, String twitterName) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitterName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return new String[]{"0", "No setup!"};
        }

        if (isTwitterSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitterNotify WHERE GID='" + guildId + "' AND NAME='" + twitterName + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[]{"0", "No setup!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "No setup!"};
    }

    /**
     * Get the TwitterNotify data.
     *
     * @param twitterName the Username of the Twitter User.
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String[]> getTwitterWebhooksByName(String twitterName) {

        ArrayList<String[]> webhooks = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (twitterName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return webhooks;
        }

        // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitterNotify WHERE NAME='" + twitterName + "'").executeQuery()) {

            // Return if there was a match.
            while (rs != null && rs.next()) {
                if (!rs.getString("CID").isEmpty() && !rs.getString("TOKEN").isEmpty())
                    webhooks.add(new String[]{rs.getString("CID"), rs.getString("TOKEN")});
            }
        } catch (Exception ignore) {
        }

        return webhooks;
    }

    /**
     * Get the all Twitter-Notifier.
     *
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String> getAllTwitterNames() {

        ArrayList<String> userNames = new ArrayList<>();

        // Creating a SQL Statement to get the Entry from the TwitterNotify Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitterNotify").executeQuery()) {

            // Return if there was a match.
            while (rs != null && rs.next()) {
                userNames.add(rs.getString("NAME"));
            }
        } catch (Exception ignore) {
        }

        return userNames;
    }

    /**
     * Get every Twitter-Notifier that has been set up for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String> getAllTwitterNames(String guildId) {

        ArrayList<String> userNames = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return userNames;
        }

        // Creating a SQL Statement to get the Entry from the TwitchNotify Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitterNotify WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            while (rs != null && rs.next()) {
                userNames.add(rs.getString("NAME"));
            }
        } catch (Exception ignore) {
        }

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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                webhookId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                authToken.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitterName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is already a Webhook set.
        if (isTwitterSetup(guildId, twitterName)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getTwitterWebhook(guildId, twitterName)[0]) && webhook.getToken().equalsIgnoreCase(getTwitterWebhook(guildId, twitterName)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM TwitterNotify WHERE GID='" + guildId + "' AND NAME='" + twitterName + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO TwitterNotify (GID, NAME, CID, TOKEN) VALUES ('" + guildId + "', '" + twitterName + "', '" + webhookId + "', '" + authToken + "');");
    }

    /**
     * Remove a Twitter Notifier entry from our Database.
     *
     * @param guildId     the ID of the Guild.
     * @param twitterName the Name of the Twitter User.
     */
    public void removeTwitterWebhook(String guildId, String twitterName) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitterName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a Webhook set.
        if (isTwitterSetup(guildId, twitterName)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook -> webhook.getToken() != null).filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getTwitterWebhook(guildId, twitterName)[0]) && webhook.getToken().equalsIgnoreCase(getTwitterWebhook(guildId, twitterName)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM TwitterNotify WHERE GID='" + guildId + "' AND NAME='" + twitterName + "'");
        }
    }

    /**
     * Check if the Twitter Webhook has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitterSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitterNotify WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Twitter Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId     the ID of the Guild.
     * @param twitterName the Username of the Twitter User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitterSetup(String guildId, String twitterName) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                twitterName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitterNotify WHERE GID='" + guildId + "' AND NAME='" + twitterName + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //endregion

    //region Roles

    //region Mute

    /**
     * Get the Mute Role ID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link String} as Role ID.
     */
    public String getMuteRole(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return "Error";
        }

        // Check if there is a role in the database.
        if (isMuteSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID from the MuteRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Return the Role ID as String if found.
                if (rs != null && rs.next()) return rs.getString("RID");
            } catch (Exception ignore) {
            }
        }

        // Return Error if there was an error OR if the role isn't in the database.
        return "Error";
    }

    /**
     * Set the MuteRole in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void setMuteRole(String guildId, String roleId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a role in the database.
        if (isMuteSetup(guildId)) {
            // Replace the entry with the new Data.
            querySQL("UPDATE MuteRoles SET RID='" + roleId + "' WHERE GID='" + guildId + "'");
        } else {
            // Add a new entry into the Database.
            querySQL("INSERT INTO MuteRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "');");
        }
    }

    /**
     * Check if a Mute Role has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isMuteSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the MuteRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //region AutoRoles

    /**
     * Get the all AutoRoles saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as List with all Role IDs.
     */
    public ArrayList<String> getAutoRoles(String guildId) {

        // Create a new ArrayList to save the Role Ids.
        ArrayList<String> roleIds = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return roleIds;
        }

        // Check if there is a role in the database.
        if (isAutoRoleSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID from the AutoRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Add the Role ID to the List if found.
                while (rs != null && rs.next()) roleIds.add(rs.getString("RID"));
            } catch (Exception ignore) {
            }
        }

        // Return the Arraylist.
        return roleIds;
    }

    /**
     * Add a AutoRole in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void addAutoRole(String guildId, String roleId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }


        // Check if there is a role in the database.
        if (!isAutoRoleSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("INSERT INTO AutoRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "');");
        }
    }

    /**
     * Remove a AutoRole from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void removeAutoRole(String guildId, String roleId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a role in the database.
        if (isAutoRoleSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("DELETE FROM AutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'");
        }
    }

    /**
     * Check if a AutoRole has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isAutoRoleSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the AutoRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    /**
     * Check if a AutoRole has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isAutoRoleSetup(String guildId, String roleId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the AutoRoles Table by the GuildID and its ID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
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
    public HashMap<Integer, String> getChatLevelRewards(String guildId) {

        // Create a new HashMap to save the Role Ids and their needed level.
        HashMap<Integer, String> rewards = new HashMap<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return rewards;
        }

        // Check if there is a role in the database.
        if (isChatLevelRewardSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID and the needed level from the ChatLevelAutoRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Add the Role ID and its needed level to the List if found.
                while (rs != null && rs.next()) rewards.put(Integer.parseInt(rs.getString("LVL")), rs.getString("RID"));
            } catch (Exception ignore) {
            }
        }

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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a role in the database.
        if (!isChatLevelRewardSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("INSERT INTO ChatLevelAutoRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "');");
        } else {
            // Update the entry.
            querySQL("UPDATE ChatLevelAutoRoles SET LVL='" + level + "' WHERE GID='" + guildId + "' AND RID='" + roleId + "'");
        }
    }

    /**
     * Remove a Chat Level Reward Role from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param level   the Level required to get this Role.
     */
    public void removeChatLevelReward(String guildId, int level) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a role in the database.
        if (isChatLevelRewardSetup(guildId)) {
            // Add a new entry into the Database.
            querySQL("DELETE FROM ChatLevelAutoRoles WHERE GID='" + guildId + "' AND LVL='" + level + "'");
        }
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the ChatLevelAutoRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId, String roleId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the ChatLevelAutoRoles Table by the GuildID and its ID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //region Voice Rewards

    /**
     * Get the all Voice Rewards saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link HashMap<>} as List with all Role IDs and the needed Level.
     */
    public HashMap<Integer, String> getVoiceLevelRewards(String guildId) {

        // Create a new HashMap to save the Role Ids and their needed level.
        HashMap<Integer, String> rewards = new HashMap<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return rewards;
        }

        // Check if there is a role in the database.
        if (isVoiceLevelRewardSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID and the needed level from the VCLevelAutoRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Add the Role ID and its needed level to the List if found.
                while (rs != null && rs.next()) rewards.put(Integer.parseInt(rs.getString("LVL")), rs.getString("RID"));
            } catch (Exception ignore) {
            }
        }

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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a role in the database.
        if (!isVoiceLevelRewardSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("INSERT INTO VCLevelAutoRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "');");
        } else {
            // Update the entry.
            querySQL("UPDATE VCLevelAutoRoles SET LVL='" + level + "' WHERE GID='" + guildId + "' AND RID='" + roleId + "'");
        }
    }

    /**
     * Remove a Voice Level Reward Role from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param level   the Level required to get this Role.
     */
    public void removeVoiceLevelReward(String guildId, int level) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is a role in the database.
        if (isVoiceLevelRewardSetup(guildId)) {
            // Add a new entry into the Database.
            querySQL("DELETE FROM VCLevelAutoRoles WHERE GID='" + guildId + "' AND LVL='" + level + "'");
        }
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the VCLevelAutoRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId, String roleId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                roleId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the RoleID from the ChatLevelAutoRoles Table by the GuildID and its ID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //endregion

    //endregion

    //region Invite

    /**
     * Get a List of every saved Invite from our Database.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as List with {@link InviteContainer}.
     */
    public ArrayList<InviteContainer> getInvites(String guildId) {

        // Create a new ArrayList to save the Invites.
        ArrayList<InviteContainer> inviteContainers = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return inviteContainers;
        }

        // Creating a SQL Statement to get the Invites from the Invites Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Invites WHERE GID='" + guildId + "'").executeQuery()) {

            // Add the Invite to the List if found.
            while (rs != null && rs.next()) inviteContainers.add(
                    new InviteContainer(rs.getString("UID"), rs.getString("GID"), rs.getString("CODE"),
                            Integer.parseInt(rs.getString("USES"))));
        } catch (Exception ignore) {
        }

        // Return an Arraylist with all Invites.
        return inviteContainers;
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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCreator.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCode.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Invite from the Invites Table by the GuildID, Invite Creator ID and Invite Code.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Invites WHERE GID='" + guildId + "' AND UID='"
                + inviteCreator + "' AND CODE='" + inviteCode + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there was an error or if it couldn't be found in our Database.
        return false;
    }

    /**
     * Remove an entry from our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param inviteCode the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCode) {
        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCode.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        querySQL("DELETE FROM Invites WHERE GID='" + guildId + "' AND CODE='" + inviteCode + "'");
    }

    /**
     * Change the data of a saved Invite or create a new entry in our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite Code.
     * @param inviteUsage   the Usage count of the Invite.
     */
    public void setInvite(String guildId, String inviteCreator, String inviteCode, int inviteUsage) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCreator.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCode.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is an entry with the same data.
        if (existsInvite(guildId, inviteCreator, inviteCode)) {
            // Update entry.
            querySQL("UPDATE Invites SET USES='" + inviteUsage + "' WHERE GID='" + guildId + "' AND UID='" + inviteCreator +
                    "' AND CODE='" + inviteCode + "'");
        } else {
            // Create new entry.
            querySQL("INSERT INTO Invites (GID, UID, USES, CODE) VALUES ('" + guildId + "', '" + inviteCode + "', '" + inviteUsage + "', " +
                    "'" + inviteCode + "');");
        }
    }

    /**
     * Remove an entry from our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCreator, String inviteCode) {
        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCreator.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCode.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        querySQL("DELETE FROM Invites WHERE GID='" + guildId + "' AND UID='" + inviteCreator + "' AND CODE='" + inviteCode + "'");
    }

    /**
     * Remove an entry from our Database.
     *
     * @param guildId       the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode    the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCreator, String inviteCode, int inviteUsage) {
        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCreator.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                inviteCode.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        querySQL("DELETE FROM Invites WHERE GID='" + guildId + "' AND UID='" + inviteCreator + "' AND CODE='" + inviteCode + "' " +
                "AND USES='" + inviteUsage + "'");
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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return "Welcome %user_mention%!\nWe wish you a great stay on %guild_name%";
        }

        if (isMessageSetup(guildId)) {
            // Creating a SQL Statement to get the Join Message from the JoinMessage Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM JoinMessage WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if found.
                if (rs != null && rs.next()) return rs.getString("MSG");
            } catch (Exception ignore) {
            }
        }

        // If No setup return default Message.
        return "Welcome %user_mention%!\nWe wish you a great stay on %guild_name%";
    }

    /**
     * Change the current Join Message of a Guild.
     *
     * @param guildId the ID of the Guild.
     * @param content the Join Message.
     */
    public void setMessage(String guildId, String content) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                content.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        if (isMessageSetup(guildId)) {
            // If there is already an entry just replace it.
            querySQL("UPDATE JoinMessage SET MSG='" + content + "' WHERE GID='" + guildId + "'");
        } else {
            // Create a new entry, if there was none.
            querySQL("INSERT INTO JoinMessage (GID, MSG) VALUE ('" + guildId + "', '" + content + "');");
        }
    }

    /**
     * Check if there is a custom Join Message set in our Database.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result. If true, then there is an entry in our Database | If false, there is no entry in our Database for that Guild.
     */
    public boolean isMessageSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Join Message from the JoinMessage Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM JoinMessage WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // If error, return false.
        return false;
    }

    //endregion

    //region Chat Protector / Word Blacklist

    /**
     * Get every Blacklisted Word saved in our Database from the Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as list with every Blacklisted Word.
     */
    public ArrayList<String> getChatProtectorWords(String guildId) {
        ArrayList<String> blacklistedWords = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return blacklistedWords;
        }

        if (isChatProtectorSetup(guildId)) {
            // Creating a SQL Statement to get the Blacklisted Words from the ChatProtector Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatProtector WHERE GID='" + guildId + "'").executeQuery()) {

                // Add if found.
                while (rs != null && rs.next()) blacklistedWords.add(rs.getString("WORD"));
            } catch (Exception ignore) {
            }
        }

        // return the ArrayList with every blacklisted Word. (Can be empty!)
        return blacklistedWords;
    }

    /**
     * Check if there is an entry in our Database for the wanted Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result. If true, there is an entry in our Database | If false, there is no entry in our Database.
     */
    public boolean isChatProtectorSetup(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to check if there is an entry in the ChatProtector Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatProtector WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        //Return false if there was an error.
        return false;
    }

    /**
     * Check if there is an entry in our Database for the wanted Guild.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word that should be checked.
     * @return {@link Boolean} as result. If true, there is an entry in our Database | If false, there is no entry in our Database.
     */
    public boolean isChatProtectorSetup(String guildId, String word) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                word.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to check if there is an entry in the ChatProtector Table by the GuildID and the Word.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatProtector WHERE GID='" + guildId + "' AND WORD='" + word + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        //Return false if there was an error.
        return false;
    }

    /**
     * Add a new Word to the blacklist for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word to be blocked.
     */
    public void addChatProtectorWord(String guildId, String word) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                word.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is already an entry for it.
        if (isChatProtectorSetup(guildId, word)) return;

        // If not then just add it.
        querySQL("INSERT INTO ChatProtector (GID, WORD) VALUES ('" + guildId + "', '" + word + "');");
    }

    public void removeChatProtectorWord(String guildId, String word) {
        // Check if there is no entry for it.
        if (!isChatProtectorSetup(guildId, word)) return;

        // If so then delete it.
        querySQL("DELETE FROM ChatProtector WHERE GID='" + guildId + "' AND WORD='" + word + "'");
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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                settingName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return new Setting("Detected SQL Injection! Please stop!", false);
        }

        // Check if there is an entry in the database.
        if (hasSetting(guildId, settingName)) {
            // Creating a SQL Statement to get the Setting in the Settings Table by the GuildID and the Setting name.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Settings WHERE GID='" + guildId + "' AND NAME='" + settingName + "'").executeQuery()) {

                // Return if found.
                if (rs != null && rs.next()) return new Setting(settingName, rs.getString("VALUE"));
            } catch (Exception ignore) {
            }
        } else {
            // Check if everything is alright with the config.
            checkSetting(guildId, settingName);
        }

        return new Setting(settingName, true);
    }

    /**
     * Get the every Setting by the Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<Setting>} which is a List with every Setting that stores every information needed.
     */
    public ArrayList<Setting> getAllSettings(String guildId) {

        ArrayList<Setting> settings = new ArrayList<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return settings;
        }

        // Creating a SQL Statement to get the Setting in the Settings Table by the GuildID and the Setting name.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Settings WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if found.
            while (rs != null && rs.next()) settings.add(new Setting(rs.getString("NAME"), rs.getObject("VALUE")));
        } catch (Exception ignore) {
        }

        // If there is no setting to be found, create every setting.
        if (settings.isEmpty()) {
            createSettings(guildId);
        }

        // Return the list.
        return settings;
    }

    /**
     * Set the Setting by the Guild and its Identifier.
     *
     * @param guildId the ID of the Guild.
     * @param setting the Setting.
     */
    public void setSetting(String guildId, Setting setting) {
        setSetting(guildId, setting.getName(), setting.getStringValue());
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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                settingName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                ("" + settingValue).matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is an entry.
        if (hasSetting(guildId, settingName)) {
            // If so update it.
            querySQL("UPDATE Settings SET VALUE='" + ("" + settingValue) + "' WHERE GID='" + guildId + "' AND NAME='" + settingName + "'");
        } else {
            // If not create a new one.
            querySQL("INSERT INTO Settings (GID, NAME, VALUE) VALUES ('" + guildId + "', '" + settingName + "', '" + ("" + settingValue) + "');");
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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                settingName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to get the Setting in the Settings Table by the GuildID and the Setting name.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Settings WHERE GID='" + guildId + "' AND NAME='" + settingName + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        return false;
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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                settingName.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

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

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Create the Chat Prefix Setting.
        if (!hasSetting(guildId, "chatprefix")) setSetting(guildId, new Setting("chatprefix", "ree!"));

        // Create Command Settings.
        for (Command command : Main.getInstance().getCommandManager().getCommands()) {

            // Skip the hidden Commands.
            if (command.getCategory() == Category.HIDDEN) continue;

            if (!hasSetting(guildId, "command_" + command.getName().toLowerCase()))
                setSetting(guildId, "command_" + command.getName().toLowerCase(), true);
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
    }

    //endregion

    //endregion

    //region Stats

    public Long getStatsCommandGlobal(String command) {

        // Check for SQL Statements to prevent SQL Injections.
        if (command.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return 0L;
        }

        // Creating a SQL Statement to get an entry in the CommandStats Table by the Command name.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM CommandStats WHERE COMMAND='" + command + "'").executeQuery()) {

            // Return if found.
            if (rs != null && rs.next()) return Long.parseLong(rs.getString("USES"));
        } catch (Exception ignore) {
        }

        return 0L;
    }

    public Long getStatsCommand(String guildId, String command) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                command.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return 0L;
        }

        // Creating a SQL Statement to get an entry in the GuildStats Table by Guild and Command name.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM GuildStats WHERE GID='" + guildId + "' AND COMMAND='" + command + "'").executeQuery()) {

            // Return if found.
            if (rs != null && rs.next()) return Long.parseLong(rs.getString("USES"));
        } catch (Exception ignore) {
        }

        return 0L;
    }

    public HashMap<String, Long> getStats(String guildId) {
        // HashMap with the Command name as key and the usage of it as value.
        HashMap<String, Long> statsMap = new HashMap<>();

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return statsMap;
        }

        // Creating a SQL Statement to get every entry in the GuildStats Table by the Guild.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM GuildStats WHERE GID='" + guildId + "' ORDER BY cast(uses as unsigned) DESC LIMIT 5;").executeQuery()) {

            // Return if found.
            while (rs != null && rs.next()) statsMap.put(rs.getString("COMMAND"), Long.parseLong(rs.getString("USES")));
        } catch (Exception ignore) {
        }

        HashMap<String, Long> sortedStatsMap = new HashMap<>();

        statsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedStatsMap.put(x.getKey(), x.getValue()));

        // Return the HashMap.
        return sortedStatsMap;
    }

    public HashMap<String, Long> getStatsGlobal() {

        // HashMap with the Command name as key and the usage of it as value.
        HashMap<String, Long> statsMap = new HashMap<>();

        // Creating a SQL Statement to get every entry in the CommandStats Table.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM CommandStats ORDER BY cast(uses as unsigned) DESC LIMIT 5;").executeQuery()) {

            // Return if found.
            while (rs != null && rs.next()) statsMap.put(rs.getString("COMMAND"), Long.parseLong(rs.getString("USES")));
        } catch (Exception ignore) {
        }

        HashMap<String, Long> sortedStatsMap = new HashMap<>();

        statsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedStatsMap.put(x.getKey(), x.getValue()));

        // Return the HashMap.
        return sortedStatsMap;
    }

    /**
     * Check if there is any saved Stats for the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isStatsSaved(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to check if there is an entry in the GuildStats Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM GuildStats WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        //Return false if there was an error.
        return false;
    }

    /**
     * Check if there is any saved Stats for the given Guild and Command.
     *
     * @param guildId the ID of the Guild.
     * @param command the Name of the Command.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isStatsSaved(String guildId, String command) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                command.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to check if there is an entry in the GuildStats Table by the GuildID and the Command name.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM GuildStats WHERE GID='" + guildId + "' AND COMMAND='" + command + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        //Return false if there was an error.
        return false;
    }

    /**
     * Check if there is any saved Stats for the given Command.
     *
     * @param command the Name of the Command.
     * @return {@link Boolean} as result. If true, there is data saved in the Database | If false, there is no data saved.
     */
    public boolean isStatsSavedGlobal(String command) {

        // Check for SQL Statements to prevent SQL Injections.
        if (command.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return false;
        }

        // Creating a SQL Statement to check if there is an entry in the CommandStats Table by the Command name.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM CommandStats WHERE COMMAND='" + command + "'").executeQuery()) {

            // Return if found.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        //Return false if there was an error.
        return false;
    }

    public void addStats(String guildId, String command) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm") ||
                command.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Check if there is an entry.
        if (isStatsSaved(guildId, command)) {
            querySQL("UPDATE GuildStats SET USES='" + (getStatsCommand(guildId, command) + 1) + "' WHERE GID='" + guildId + "' AND COMMAND='" + command + "'");
        } else {
            querySQL("INSERT INTO GuildStats (GID, COMMAND, USES) VALUES ('" + guildId + "', '" + command + "', '1')");
        }

        // Check if there is an entry.
        if (isStatsSavedGlobal(command)) {
            querySQL("UPDATE CommandStats SET USES='" + (getStatsCommandGlobal(command) + 1) + "' WHERE COMMAND='" + command + "'");
        } else {
            querySQL("INSERT INTO CommandStats (COMMAND, USES) VALUES ('" + command + "', '1')");
        }
    }

    //endregion

    //region Data delete

    /**
     * Delete Data saved in our Database by the given Guild ID.
     *
     * @param guildId the ID of the Guild.
     */
    public void deleteAllData(String guildId) {

        // Check for SQL Statements to prevent SQL Injections.
        if (guildId.matches("/\"((SELECT|DELETE|UPDATE|INSERT INTO) (\\*|[A-Z0-9_]+) (FROM) ([A-Z0-9_]+))( (WHERE) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?|\\$[A-Z]{1}[A-Z_]+)( (AND) ([A-Z0-9_]+) (=|<|>|>=|<=|==|!=) (\\?))?)?\"/igm")) {
            return;
        }

        // Go through every Table. And delete every entry with the Guild ID.
        sqlConnector.getTables().forEach((s, s2) -> querySQL("DELETE FROM " + s + " WHERE GID='" + guildId + "'"));
    }

    //endregion

    /**
     * Send an SQL-Query to SQL-Server.
     *
     * @param sqlQuery the SQL-Query.
     */
    public void querySQL(String sqlQuery) {
        if (!sqlConnector.IsConnected()) return;

        try (Statement statement = sqlConnector.getConnection().createStatement()) {
            statement.executeUpdate(sqlQuery);
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Couldn't send Query to SQL-Server ( " + sqlQuery + " )", exception);
        }
    }
}
