package de.presti.ree6.sql.entities.stats;

import de.presti.ree6.sql.base.annotations.*;
import de.presti.ree6.sql.base.data.SQLEntity;

/**
 * SQL Entity for the Stats.
 */
@Table(name = "ChannelStats")
public class ChannelStats extends SQLEntity {

    // TODO:: document.

    @Property(name = "gid")
    private String guildId;

    @Property(name = "memberChannelId")
    private String memberStatsChannelId;

    @Property(name = "onlyRealMemberChannelId")
    private String realMemberStatsChannelId;

    @Property(name = "botMemberChannelId")
    private String botMemberStatsChannelId;

    @Property(name = "twitterFollowerChannelId")
    private String twitterFollowerChannelId;

    @Property(name = "twitterFollowerChannelUsername")
    private String twitterFollowerChannelUsername;

    @Property(name = "instagramFollowerChannelId")
    private String instagramFollowerChannelId;

    @Property(name = "instagramFollowerChannelUsername")
    private String instagramFollowerChannelUsername;

    @Property(name = "twitchFollowerChannelId")
    private String twitchFollowerChannelId;

    @Property(name = "twitchFollowerChannelUsername")
    private String twitchFollowerChannelUsername;

    @Property(name = "youtubeSubscribersChannelId")
    private String youtubeSubscribersChannelId;

    @Property(name = "youtubeSubscribersChannelUsername")
    private String youtubeSubscribersChannelUsername;

    @Property(name = "subredditMemberChannelId")
    private String subredditMemberChannelId;

    @Property(name = "subredditMemberChannelSubredditName")
    private String subredditMemberChannelSubredditName;

    public ChannelStats() {
    }

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

    public String getGuildId() {
        return guildId;
    }

    public String getMemberStatsChannelId() {
        return memberStatsChannelId;
    }

    public String getRealMemberStatsChannelId() {
        return realMemberStatsChannelId;
    }

    public String getBotMemberStatsChannelId() {
        return botMemberStatsChannelId;
    }

    public String getTwitterFollowerChannelId() {
        return twitterFollowerChannelId;
    }

    public String getTwitterFollowerChannelUsername() {
        return twitterFollowerChannelUsername;
    }

    public String getInstagramFollowerChannelId() {
        return instagramFollowerChannelId;
    }

    public String getInstagramFollowerChannelUsername() {
        return instagramFollowerChannelUsername;
    }

    public String getTwitchFollowerChannelId() {
        return twitchFollowerChannelId;
    }

    public String getTwitchFollowerChannelUsername() {
        return twitchFollowerChannelUsername;
    }

    public String getYoutubeSubscribersChannelId() {
        return youtubeSubscribersChannelId;
    }

    public String getYoutubeSubscribersChannelUsername() {
        return youtubeSubscribersChannelUsername;
    }

    public String getSubredditMemberChannelId() {
        return subredditMemberChannelId;
    }

    public String getSubredditMemberChannelSubredditName() {
        return subredditMemberChannelSubredditName;
    }

    public void setMemberStatsChannelId(String memberStatsChannelId) {
        this.memberStatsChannelId = memberStatsChannelId;
    }

    public void setRealMemberStatsChannelId(String realMemberStatsChannelId) {
        this.realMemberStatsChannelId = realMemberStatsChannelId;
    }

    public void setBotMemberStatsChannelId(String botMemberStatsChannelId) {
        this.botMemberStatsChannelId = botMemberStatsChannelId;
    }

    public void setTwitterFollowerChannelId(String twitterFollowerChannelId) {
        this.twitterFollowerChannelId = twitterFollowerChannelId;
    }

    public void setTwitterFollowerChannelUsername(String twitterFollowerChannelUsername) {
        this.twitterFollowerChannelUsername = twitterFollowerChannelUsername;
    }

    public void setInstagramFollowerChannelId(String instagramFollowerChannelId) {
        this.instagramFollowerChannelId = instagramFollowerChannelId;
    }

    public void setInstagramFollowerChannelUsername(String instagramFollowerChannelUsername) {
        this.instagramFollowerChannelUsername = instagramFollowerChannelUsername;
    }

    public void setTwitchFollowerChannelId(String twitchFollowerChannelId) {
        this.twitchFollowerChannelId = twitchFollowerChannelId;
    }

    public void setTwitchFollowerChannelUsername(String twitchFollowerChannelUsername) {
        this.twitchFollowerChannelUsername = twitchFollowerChannelUsername;
    }

    public void setYoutubeSubscribersChannelId(String youtubeSubscribersChannelId) {
        this.youtubeSubscribersChannelId = youtubeSubscribersChannelId;
    }

    public void setYoutubeSubscribersChannelUsername(String youtubeSubscribersChannelUsername) {
        this.youtubeSubscribersChannelUsername = youtubeSubscribersChannelUsername;
    }

    public void setSubredditMemberChannelId(String subredditMemberChannelId) {
        this.subredditMemberChannelId = subredditMemberChannelId;
    }

    public void setSubredditMemberChannelSubredditName(String subredditMemberChannelSubredditName) {
        this.subredditMemberChannelSubredditName = subredditMemberChannelSubredditName;
    }
}
