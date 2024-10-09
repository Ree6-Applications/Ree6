package de.presti.ree6.module.news;

/**
 * Announcement entity class to store information about an announcement.
 *
 * @param id      The ID of the announcement.
 * @param title   The title of the announcement.
 * @param content The content of the announcement.
 */
public record Announcement(String id, String title, String content) {

    /**
     * Get the ID of the announcement.
     * @return The ID of the announcement.
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Get the title of the announcement.
     *
     * @return The title of the announcement.
     */
    @Override
    public String title() {
        return title;
    }

    /**
     * Get the content of the announcement.
     * @return The content of the announcement.
     */
    @Override
    public String content() {
        return content;
    }
}
