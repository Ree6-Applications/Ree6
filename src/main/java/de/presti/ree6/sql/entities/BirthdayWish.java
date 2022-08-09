package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.entitis.SQLEntity;

import java.util.Date;

/**
 * This class is used to represent a Birthday-Wish, in our Database.
 */
@Table(name = "BirthdayWish")
public class BirthdayWish extends SQLEntity {

    /**
     * The Guild ID.
     */
    @Property(name = "gid")
    String guildId;

    /**
     * The Channel ID.
     */
    @Property(name = "cid")
    String channelId;

    /**
     * The User ID.
     */
    @Property(name = "uid")
    String userId;

    /**
     * The Birthday.
     */
    @Property(name = "birthday", keepOriginalValue = false)
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
