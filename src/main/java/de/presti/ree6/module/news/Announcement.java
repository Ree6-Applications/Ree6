package de.presti.ree6.module.news;

/**
 * Announcement entity class to store information about an announcement.
 *
 * @param id      The ID of the announcement.
 * @param title   The title of the announcement.
 * @param content The content of the announcement.
 */
public record Announcement(String id, String title, String content) {
}
