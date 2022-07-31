package de.presti.ree6.sql.entities.level;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.annotations.Table;

/**
 * Utility class to store information about a Users
 * Experience and their Level.
 */
@Table(name = "VCLevel")
public class VoiceUserLevel extends UserLevel {

    /**
     * Constructor.
     */
    public VoiceUserLevel() {
    }

    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     */
    public VoiceUserLevel(String guildId, String userId, long experience) {
        super(guildId, userId, experience, Main.getInstance().getSqlConnector().getSqlWorker().getAllVoiceLevelSorted(guildId).indexOf(userId));
    }


    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     * @param level      his Level.
     */
    public VoiceUserLevel(String guildId, String userId, long experience, long level) {
        super(guildId, userId, experience, level, Main.getInstance().getSqlConnector().getSqlWorker().getAllVoiceLevelSorted(guildId).indexOf(userId));
    }
}
