package de.presti.ree6.sql.entities;

import jakarta.persistence.*;

/**
 * Class that represents the OptOut Database Entity.
 */
@Entity
@Table(name = "Opt_out")
public class OptOut {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * The ID of the Guild.
     */
    @Column(name = "gid")
    String guildId;

    /**
     * The ID of the User.
     */
    @Column(name = "uid")
    String userId;

    /**
     * Constructor.
     */
    public OptOut() {
    }

    /**
     * Constructor.
     *
     * @param guildId the Guild ID.
     * @param userId the User ID.
     */
    public OptOut(String guildId, String userId) {
        this.guildId = guildId;
        this.userId = userId;
    }

    /**
     * The ID of the Guild.
     * @return the ID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Set the ID of the Guild.
     * @param guildId new ID.
     */
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    /**
     * The ID of the User.
     * @return the ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the ID of the User.
     * @param userId new ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
