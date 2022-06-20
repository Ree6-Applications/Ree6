package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;

/**
 * SQL Entity for the Webhooks.
 */
public class Webhook {

    @Property(name = "gid", primary = true)
    private String guildId;

    @Property(name = "cid")
    private String channelId;

    @Property(name = "token")
    private String token;

    public Webhook(String guildId, String channelId, String token) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.token = token;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getToken() {
        return token;
    }
}
