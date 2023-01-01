package de.presti.ree6.news;

/**
 * Announcement entity class to store information about an announcement.
 *
 * @param id      The ID of the announcement.
 * @param title   The title of the announcement.
 * @param content The content of the announcement.
 */
public record Announcement(String id, String title, String content) {

    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String content() {
        return content;
    }
}
