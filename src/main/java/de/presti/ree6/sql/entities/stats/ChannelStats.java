package de.presti.ree6.sql.entities.stats;

import jakarta.persistence.*;

// TODO:: split all these in separate classes to keep everything more organized.

/**
 * SQL Entity for the Stats.
 */
@Entity
@Table(name = "ChannelStats")
public class ChannelStats {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * The Guild ID.
     */
    @Column(name = "gid")
    private String guildId;

    /**
     * The Channel ID for overall Members of the Guild.
     */
    @Column(name = "memberChannelId")
    private String memberStatsChannelId;

    /**
     * The Channel ID for real Members of the Guild.
     */
    @Column(name = "onlyRealMemberChannelId")
    private String realMemberStatsChannelId;

    /**
     * The Channel ID for bot Members of the Guild.
     */
    @Column(name = "botMemberChannelId")
    private String botMemberStatsChannelId;

    /**
     * The Channel ID for Twitter Stats of the Guild.
     */
    @Column(name = "twitterFollowerChannelId")
    private String twitterFollowerChannelId;

    /**
     * The username for Twitter Stats of the Guild.
     */
    @Column(name = "twitterFollowerChannelUsername")
    private String twitterFollowerChannelUsername;

    /**
     * The Channel ID for Instagram Stats of the Guild.
     */
    @Column(name = "instagramFollowerChannelId")
    private String instagramFollowerChannelId;

    /**
     * The username for Instagram Stats of the Guild.
     */
    @Column(name = "instagramFollowerChannelUsername")
    private String instagramFollowerChannelUsername;

    /**
     * The Channel ID for Twitch Stats of the Guild.
     */
    @Column(name = "twitchFollowerChannelId")
    private String twitchFollowerChannelId;

    /**
     * The username for Twitch Stats of the Guild.
     */
    @Column(name = "twitchFollowerChannelUsername")
    private String twitchFollowerChannelUsername;

    /**
     * The Channel ID for YouTube Stats of the Guild.
     */
    @Column(name = "youtubeSubscribersChannelId")
    private String youtubeSubscribersChannelId;

    /**
     * The username for YouTube Stats of the Guild.
     */
    @Column(name = "youtubeSubscribersChannelUsername")
    private String youtubeSubscribersChannelUsername;

    /**
     * The Channel ID for Reddit Stats of the Guild.
     */
    @Column(name = "subredditMemberChannelId")
    private String subredditMemberChannelId;

    /**
     * The subreddit name for Reddit Stats of the Guild.
     */
    @Column(name = "subredditMemberChannelSubredditName")
    private String subredditMemberChannelSubredditName;

    /**
     * Constructor.
     */
    public ChannelStats() {
    }

    /**
     * Constructor.
     * @param guildId the Guild ID.
     * @param memberStatsChannelId the Channel ID for overall Members of the Guild.
     * @param realMemberStatsChannelId the Channel ID for real Members of the Guild.
     * @param botMemberStatsChannelId the Channel ID for bot Members of the Guild.
     * @param twitterFollowerChannelId the Channel ID for Twitter Stats of the Guild.
     * @param twitterFollowerChannelUsername the username for Twitter Stats of the Guild.
     * @param instagramFollowerChannelId the Channel ID for Instagram Stats of the Guild.
     * @param instagramFollowerChannelUsername the username for Instagram Stats of the Guild.
     * @param twitchFollowerChannelId the Channel ID for Twitch Stats of the Guild.
     * @param twitchFollowerChannelUsername the username for Twitch Stats of the Guild.
     * @param youtubeSubscribersChannelId the Channel ID for YouTube Stats of the Guild.
     * @param youtubeSubscribersChannelUsername the username for YouTube Stats of the Guild.
     * @param subredditMemberChannelId the Channel ID for Reddit Stats of the Guild.
     * @param subredditMemberChannelSubredditName the subreddit name for Reddit Stats of the Guild.
     */
    public ChannelStats(String guildId,
                        String memberStatsChannelId,
                        String realMemberStatsChannelId,
                        String botMemberStatsChannelId,
                        String twitterFollowerChannelId,
                        String twitterFollowerChannelUsername,
                        String instagramFollowerChannelId,
                        String instagramFollowerChannelUsername,
                        String twitchFollowerChannelId,
                        String twitchFollowerChannelUsername,
                        String youtubeSubscribersChannelId,
                        String youtubeSubscribersChannelUsername,
                        String subredditMemberChannelId,
                        String subredditMemberChannelSubredditName) {
        this.guildId = guildId;
        this.memberStatsChannelId = memberStatsChannelId;
        this.realMemberStatsChannelId = realMemberStatsChannelId;
        this.botMemberStatsChannelId = botMemberStatsChannelId;
        this.twitterFollowerChannelId = twitterFollowerChannelId;
        this.twitterFollowerChannelUsername = twitterFollowerChannelUsername;
        this.instagramFollowerChannelId = instagramFollowerChannelId;
        this.instagramFollowerChannelUsername = instagramFollowerChannelUsername;
        this.twitchFollowerChannelId = twitchFollowerChannelId;
        this.twitchFollowerChannelUsername = twitchFollowerChannelUsername;
        this.youtubeSubscribersChannelId = youtubeSubscribersChannelId;
        this.youtubeSubscribersChannelUsername = youtubeSubscribersChannelUsername;
        this.subredditMemberChannelId = subredditMemberChannelId;
        this.subredditMemberChannelSubredditName = subredditMemberChannelSubredditName;
    }

    /**
     * Get the Guild ID.
     * @return the Guild ID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the Channel ID for overall Members of the Guild.
     * @return the Channel ID for overall Members of the Guild.
     */
    public String getMemberStatsChannelId() {
        return memberStatsChannelId;
    }

    /**
     * Get the Channel ID for real Members of the Guild.
     * @return the Channel ID for real Members of the Guild.
     */
    public String getRealMemberStatsChannelId() {
        return realMemberStatsChannelId;
    }

    /**
     * Get the Channel ID for bot Members of the Guild.
     * @return the Channel ID for bot Members of the Guild.
     */
    public String getBotMemberStatsChannelId() {
        return botMemberStatsChannelId;
    }

    /**
     * Get the Channel ID for Twitter Stats of the Guild.
     * @return the Channel ID for Twitter Stats of the Guild.
     */
    public String getTwitterFollowerChannelId() {
        return twitterFollowerChannelId;
    }

    /**
     * Get the username for Twitter Stats of the Guild.
     * @return the username for Twitter Stats of the Guild.
     */
    public String getTwitterFollowerChannelUsername() {
        return twitterFollowerChannelUsername;
    }

    /**
     * Get the Channel ID for Instagram Stats of the Guild.
     * @return the Channel ID for Instagram Stats of the Guild.
     */
    public String getInstagramFollowerChannelId() {
        return instagramFollowerChannelId;
    }

    /**
     * Get the username for Instagram Stats of the Guild.
     * @return the username for Instagram Stats of the Guild.
     */
    public String getInstagramFollowerChannelUsername() {
        return instagramFollowerChannelUsername;
    }

    /**
     * Get the Channel ID for Twitch Stats of the Guild.
     * @return the Channel ID for Twitch Stats of the Guild.
     */
    public String getTwitchFollowerChannelId() {
        return twitchFollowerChannelId;
    }

    /**
     * Get the username for Twitch Stats of the Guild.
     * @return the username for Twitch Stats of the Guild.
     */
    public String getTwitchFollowerChannelUsername() {
        return twitchFollowerChannelUsername;
    }

    /**
     * Get the Channel ID for YouTube Stats of the Guild.
     * @return the Channel ID for YouTube Stats of the Guild.
     */
    public String getYoutubeSubscribersChannelId() {
        return youtubeSubscribersChannelId;
    }

    /**
     * Get the username for YouTube Stats of the Guild.
     * @return the username for YouTube Stats of the Guild.
     */
    public String getYoutubeSubscribersChannelUsername() {
        return youtubeSubscribersChannelUsername;
    }

    /**
     * Get the Channel ID for Reddit Stats of the Guild.
     * @return the Channel ID for Reddit Stats of the Guild.
     */
    public String getSubredditMemberChannelId() {
        return subredditMemberChannelId;
    }

    /**
     * Get the subreddit name for Reddit Stats of the Guild.
     * @return the subreddit name for Reddit Stats of the Guild.
     */
    public String getSubredditMemberChannelSubredditName() {
        return subredditMemberChannelSubredditName;
    }

    /**
     * Set the Channel ID for overall Members of the Guild.
     * @param memberStatsChannelId the Channel ID for overall Members of the Guild.
     */
    public void setMemberStatsChannelId(String memberStatsChannelId) {
        this.memberStatsChannelId = memberStatsChannelId;
    }

    /**
     * Set the Channel ID for real Members of the Guild.
     * @param realMemberStatsChannelId the Channel ID for real Members of the Guild.
     */
    public void setRealMemberStatsChannelId(String realMemberStatsChannelId) {
        this.realMemberStatsChannelId = realMemberStatsChannelId;
    }

    /**
     * Set the Channel ID for bot Members of the Guild.
     * @param botMemberStatsChannelId the Channel ID for bot Members of the Guild.
     */
    public void setBotMemberStatsChannelId(String botMemberStatsChannelId) {
        this.botMemberStatsChannelId = botMemberStatsChannelId;
    }

    /**
     * Set the Channel ID for Twitter Stats of the Guild.
     * @param twitterFollowerChannelId the Channel ID for Twitter Stats of the Guild.
     */
    public void setTwitterFollowerChannelId(String twitterFollowerChannelId) {
        this.twitterFollowerChannelId = twitterFollowerChannelId;
    }

    /**
     * Set the username for Twitter Stats of the Guild.
     * @param twitterFollowerChannelUsername the username for Twitter Stats of the Guild.
     */
    public void setTwitterFollowerChannelUsername(String twitterFollowerChannelUsername) {
        this.twitterFollowerChannelUsername = twitterFollowerChannelUsername;
    }

    /**
     * Set the Channel ID for Instagram Stats of the Guild.
     * @param instagramFollowerChannelId the Channel ID for Instagram Stats of the Guild.
     */
    public void setInstagramFollowerChannelId(String instagramFollowerChannelId) {
        this.instagramFollowerChannelId = instagramFollowerChannelId;
    }

    /**
     * Set the username for Instagram Stats of the Guild.
     * @param instagramFollowerChannelUsername the username for Instagram Stats of the Guild.
     */
    public void setInstagramFollowerChannelUsername(String instagramFollowerChannelUsername) {
        this.instagramFollowerChannelUsername = instagramFollowerChannelUsername;
    }

    /**
     * Set the Channel ID for Twitch Stats of the Guild.
     * @param twitchFollowerChannelId the Channel ID for Twitch Stats of the Guild.
     */
    public void setTwitchFollowerChannelId(String twitchFollowerChannelId) {
        this.twitchFollowerChannelId = twitchFollowerChannelId;
    }

    /**
     * Set the username for Twitch Stats of the Guild.
     * @param twitchFollowerChannelUsername the username for Twitch Stats of the Guild.
     */
    public void setTwitchFollowerChannelUsername(String twitchFollowerChannelUsername) {
        this.twitchFollowerChannelUsername = twitchFollowerChannelUsername;
    }

    /**
     * Set the Channel ID for YouTube Stats of the Guild.
     * @param youtubeSubscribersChannelId the Channel ID for YouTube Stats of the Guild.
     */
    public void setYoutubeSubscribersChannelId(String youtubeSubscribersChannelId) {
        this.youtubeSubscribersChannelId = youtubeSubscribersChannelId;
    }

    /**
     * Set the username for YouTube Stats of the Guild.
     * @param youtubeSubscribersChannelUsername the username for YouTube Stats of the Guild.
     */
    public void setYoutubeSubscribersChannelUsername(String youtubeSubscribersChannelUsername) {
        this.youtubeSubscribersChannelUsername = youtubeSubscribersChannelUsername;
    }

    /**
     * Set the Channel ID for Reddit Stats of the Guild.
     * @param subredditMemberChannelId the Channel ID for Reddit Stats of the Guild.
     */
    public void setSubredditMemberChannelId(String subredditMemberChannelId) {
        this.subredditMemberChannelId = subredditMemberChannelId;
    }

    /**
     * Set the subreddit name for Reddit Stats of the Guild.
     * @param subredditMemberChannelSubredditName the subreddit name for Reddit Stats of the Guild.
     */
    public void setSubredditMemberChannelSubredditName(String subredditMemberChannelSubredditName) {
        this.subredditMemberChannelSubredditName = subredditMemberChannelSubredditName;
    }
}
