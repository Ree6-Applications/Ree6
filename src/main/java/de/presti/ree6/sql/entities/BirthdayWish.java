package de.presti.ree6.sql.entities;

import jakarta.persistence.*;

import java.util.Date;

/**
 * This class is used to represent a Birthday-Wish, in our Database.
 */
@Entity
@Table(name = "BirthdayWish")
public class BirthdayWish {


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
    String guildId;

    /**
     * The Channel ID.
     */
    @Column(name = "cid")
    String channelId;

    /**
     * The User ID.
     */
    @Column(name = "uid")
    String userId;

    /**
     * The Birthday.
     */
    @Column(name = "birthday")
    Date birthdate;

    /**
     * Constructor.
     */
    public BirthdayWish() {
    }

    /**
     * Constructor.
     *
     * @param guildId  the Guild ID.
     * @param channelId the Channel ID.
     * @param userId the User ID.
     * @param birthdate the Birthday.
     */
    public BirthdayWish(String guildId, String channelId, String userId, Date birthdate) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        this.birthdate = birthdate;
    }

    /**
     * Get the Guild ID.
     *
     * @return the Guild ID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the Channel ID.
     *
     * @return the Channel ID.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Get the User ID.
     *
     * @return the User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the Birthday.
     *
     * @return the Birthday.
     */
    public Date getBirthdate() {
        return birthdate;
    }
}
