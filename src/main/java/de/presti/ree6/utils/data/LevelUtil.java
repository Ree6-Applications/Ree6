package de.presti.ree6.utils.data;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.sql.entities.level.UserLevel;
import de.presti.ree6.sql.entities.level.VoiceUserLevel;

/**
 * Utility used to determine data for levels.
 */
public class LevelUtil {

    /**
     * Calculate on which Level you would be by your experience.
     *
     * @param userLevel the userLevel.
     * @return which Level.
     */
    public static long calculateLevel(UserLevel userLevel) {
        int i = 0;
        while (true) {
            long requiredXP = getTotalExperienceForLevel(i, userLevel);
            if (userLevel.getExperience() <= requiredXP) return (i == 0 ? 1 : i - 1);
            i++;
        }
    }

    /**
     * Calculate on which Level you would be by your experience.
     *
     * @param userLevel the UserLevel.
     * @param experience the experience.
     * @return which Level.
     */
    public static long calculateLevel(UserLevel userLevel, long experience) {
        int i = 0;
        while (true) {
            long requiredXP = getTotalExperienceForLevel(i, userLevel);
            if (experience <= requiredXP) return (i == 0 ? 1 : i - 1);
            i++;
        }
    }

    /**
     * Get the needed Experience for the next Level.
     *
     * @param level The level.
     * @param userLevel the UserLevel.
     * @return the needed Experience.
     */
    public static long getTotalExperienceForLevel(long level, UserLevel userLevel) {
        long requiredXP = 0;
        for (int i = 0; i <= level; i++) {
            requiredXP += getExperienceForLevel(i, userLevel);
        }
        return requiredXP;
    }

    /**
     * Get the needed Experience for the next Level.
     *
     * @param level The level.
     * @param userLevel the UserLevel.
     * @return the needed Experience.
     */
    public static long getExperienceForLevel(long level, UserLevel userLevel) {
        if (userLevel instanceof ChatUserLevel) {
            return (long) (1000 + (1000 * Math.pow(level, 0.55)));
        } else if (userLevel instanceof VoiceUserLevel) {
            return (long) (1000 + (1000 * Math.pow(level, 1.05)));
        }
        return level;
    }

    /**
     * Get the current Progress of the User.
     *
     * @param userLevel the UserLevel.
     * @return the Progress.
     */
    public static double getProgress(UserLevel userLevel) {
        return (int) ((userLevel.getExperience() * 100) / getTotalExperienceForLevel(userLevel.getLevel() + 1, userLevel));
    }

    /**
     * Retrieve the current Rank of a User.
     * @param userLevel the UserLevel.
     * @return the current Rank.
     */
    public static int getCurrentRank(UserLevel userLevel) {
        if (userLevel instanceof ChatUserLevel) {
            return Main.getInstance().getSqlConnector().getSqlWorker().getAllChatLevelSorted(userLevel.getGuildId()).indexOf(userLevel.getUserId()) + 1;
        } else if (userLevel instanceof VoiceUserLevel) {
            return Main.getInstance().getSqlConnector().getSqlWorker().getAllVoiceLevelSorted(userLevel.getGuildId()).indexOf(userLevel.getUserId()) + 1;
        }

        return 0;
    }
}
