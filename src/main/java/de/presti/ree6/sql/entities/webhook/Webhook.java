package de.presti.ree6.sql.entities.webhook;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.entitis.SQLEntity;

/**
 * SQL Entity for the Webhooks.
 */
public class Webhook extends SQLEntity {

    /**
     * The GuildID of the Webhook.
     */
    @Property(name = "gid")
    private String guildId;

    /**
     * The ChannelID of the Webhook.
     */
    @Property(name = "cid")
    private String channelId;

    /**
     * The Token of the Webhook.
     */
    @Property(name = "token")
    private String token;

    /**
     * Constructor.
     */
    public Webhook() {
    }

    /**
     * Constructor.
     *
     * @param guildId   The GuildID of the Webhook.
     * @param channelId The ChannelID of the Webhook.
     * @param token     The Token of the Webhook.
     */
    public Webhook(String guildId, String channelId, String token) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.token = token;
    }

    /**
     * Get the GuildID of the Webhook.
     *
     * @return {@link String} as GuildID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the ChannelID of the Webhook.
     *
     * @return {@link String} as ChannelID.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Get the Token of the Webhook.
     *
     * @return {@link String} as Token.
     */
    public String getToken() {
        return token;
    }
}
