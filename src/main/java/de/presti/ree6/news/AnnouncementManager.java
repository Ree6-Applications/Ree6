package de.presti.ree6.news;

import de.presti.ree6.bot.BotConfig;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Announcement Manager used to store and manage announcements.
 */
public class AnnouncementManager {

    /**
     * List of all announcements.
     */
    @Getter
    private static final List<Announcement> announcementList = new ArrayList<>();

    /**
     * HashMap used to store guild ids and their received announcements.
     */
    @Getter
    private static final HashMap<Long, List<String>> receivedAnnouncements = new HashMap<>();

    /**
     * Method used to add an announcement to the list.
     *
     * @param announcement Announcement to add.
     */
    public static void addAnnouncement(Announcement announcement) {
        if (!BotConfig.isModuleActive("news")) return;
        announcementList.add(announcement);
    }

    /**
     * Method used to check if a Guild already received an announcement.
     *
     * @param guildId        The ID of the Guild.
     * @param announcementId The ID of the Announcement.
     * @return True if the Guild already received the announcement.
     */
    public static boolean hasReceivedAnnouncement(long guildId, String announcementId) {
        if (!BotConfig.isModuleActive("news")) return true;

        if (receivedAnnouncements.containsKey(guildId)) {
            return receivedAnnouncements.get(guildId).contains(announcementId);
        }

        return false;
    }

    /**
     * Method used to add an announcement to the received announcements of a Guild.
     *
     * @param guildId        The ID of the Guild.
     * @param announcementId The ID of the Announcement.
     */
    public static void addReceivedAnnouncement(long guildId, String announcementId) {
        if (receivedAnnouncements.containsKey(guildId)) {
            receivedAnnouncements.get(guildId).add(announcementId);
        } else {
            List<String> announcementIds = new ArrayList<>();
            announcementIds.add(announcementId);
            receivedAnnouncements.put(guildId, announcementIds);
        }
    }

    /**
     * Method used to remove announcements from the list.
     *
     * @param id The ID of the announcement to remove.
     */
    public static void removeAnnouncement(String id) {
        announcementList.removeIf(announcement -> announcement.id().equalsIgnoreCase(id));
    }
}
