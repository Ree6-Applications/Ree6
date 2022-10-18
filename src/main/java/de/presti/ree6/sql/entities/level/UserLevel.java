package de.presti.ree6.sql.entities.level;

import de.presti.ree6.utils.data.LevelUtil;
import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.User;

/**
 * Base class for the UserLevel.
 */
@MappedSuperclass
public class UserLevel {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * The User.
     */
    @Transient
    User user;

    /**
     * The experience of the User.
     */
    @Column(name = "xp")
    long experience;

    /**
     * The level of the User.
     */
    @Transient
    long level = -1;

    /**
     * The Rank of the User.
     */
    @Transient
    int rank = -1;

    /**
     * Constructor.
     */
    public UserLevel() {
    }

    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     * @param rank       the Rank of the User.
     */
    public UserLevel(String guildId, String userId, long experience, int rank) {
        this.guildId = guildId;
        this.userId = userId;
        this.experience = experience;
        level = LevelUtil.calculateLevel(this, experience);
        this.rank = rank;
    }


    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     * @param level      his Level.
     * @param rank       the Rank of the User.
     */
    public UserLevel(String guildId, String userId, long experience, long level, int rank) {
        this.guildId = guildId;
        this.userId = userId;
        this.experience = experience;
        this.level = level;
        this.rank = rank;
    }

    /**
     * Get the ID of the wanted User.
     *
     * @return the ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Change the ID of the User.
     *
     * @param userId the new ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the wanted User.
     *
     * @return the User.
     */
    public User getUser() {
        return user;
    }

    /**
     * Change the User Entity of the User.
     *
     * @param user the new User.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get the XP count of the User.
     *
     * @return the XP count.
     */
    public long getExperience() {
        return experience;
    }

    /**
     * Change the XP count.
     *
     * @param experience new XP count.
     */
    public void setExperience(long experience) {
        this.experience = experience;
    }

    /**
     * Get the Level of the User.
     *
     * @return the level.
     */
    public long getLevel() {
        if (level == -1) {
            level = LevelUtil.calculateLevel(this);
        }
        return level;
    }

    /**
     * Change the Level of the User.
     *
     * @param level the new level.
     */
    public void setLevel(long level) {
        this.level = level;
    }

    /**
     * Get the current Rank of the User.
     *
     * @return the current rank.
     */
    public int getRank() {
        if (rank == -1) {
            rank = LevelUtil.getCurrentRank(this);
        }

        return rank;
    }

    /**
     * Get the GuildId.
     * @return the GuildId.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Added experience to the UserLevel.
     *
     * @param experience the experience that should be added.
     * @return true, if you leveled up | false, if not.
     */
    public boolean addExperience(long experience) {
        long currentLevel = getLevel();

        long newExperience = getExperience() + experience;

        long calculatedLevel = LevelUtil.calculateLevel(this, newExperience);

        setExperience(newExperience);

        if (calculatedLevel > currentLevel) {
            level = calculatedLevel;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the needed Experience for the next Level.
     *
     * @return the needed Experience.
     */
    public long getTotalExperienceForNextLevel() {
        return getTotalExperienceForLevel(getLevel() + 1);
    }


    /**
     * Get the needed Experience for the next Level.
     *
     * @param level The level.
     * @return the needed Experience.
     */
    public long getTotalExperienceForLevel(long level) {
        return LevelUtil.getTotalExperienceForLevel(level, this);
    }

    /**
     * Get the current Progress of the User.
     *
     * @return the Progress.
     */
    public double getProgress() {
        return LevelUtil.getProgress(this);
    }

    /**
     * Get the current Experience but formatted.
     *
     * @return a formatted version of the current Experience.
     */
    public String getFormattedExperience() {
        return getFormattedExperience(getExperience());
    }

    /**
     * Get the Experience but formatted.
     *
     * @param experience the Experience that should be formatted.
     * @return a formatted version of the Experience.
     */
    public String getFormattedExperience(long experience) {
        String end;

        if (experience >= 1000000000000L) {
            end = ((experience / 1000000000000L) + "").replace("l", "") + "mil";
        } else if (experience >= 1000000000) {
            end = ((experience / 1000000000) + "").replace("l", "") + "mil";
        } else if (experience >= 1000000) {
            end = ((experience / 1000000) + "").replace("l", "") + "mio";
        } else if (experience >= 1000) {
            end = ((experience / 1000) + "").replace("l", "") + "k";
        } else {
            end = "" + getExperience();
        }

        return end;
    }
}
