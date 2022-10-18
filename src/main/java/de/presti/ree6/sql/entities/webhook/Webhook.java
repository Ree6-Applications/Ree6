package de.presti.ree6.sql.entities.webhook;

import jakarta.persistence.*;

/**
 * SQL Entity for the Webhooks.
 */
@MappedSuperclass
public class Webhook {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * The GuildID of the Webhook.
     */
    @Column(name = "gid")
    private String guildId;

    /**
     * The ChannelID of the Webhook.
     */
    @Column(name = "cid")
    private String channelId;

    /**
     * The Token of the Webhook.
     */
    @Column(name = "token")
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
