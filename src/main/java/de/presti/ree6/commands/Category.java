package de.presti.ree6.commands;

/**
 * Different Categories for the commands.
 */
public enum Category {

    /**
     * Category used for informativ Commands.
     */
    INFO("re_icon_info",1019221661070917632L, false, "category.info"),
    /**
     * Category used for moderativ Commands.
     */
    MOD("re_icon_mod",1019221710932803624L, false, "category.moderation"),
    /**
     * Category used for music Commands.
     */
    MUSIC("re_icon_music",1019221781762023575L, false, "category.music"),
    /**
     * Category used for fun Commands.
     */
    FUN("re_icon_fun",1019221814670532628L, false, "category.fun"),
    /**
     * Category used for level Commands.
     */
    LEVEL("re_icon_level",1019221845972635809L, false, "category.level"),
    /**
     * Category used for community Commands.
     */
    COMMUNITY("re_icon_community",1019221884686057552L, false, "category.community"),
    /**
     * Category used for NSFW Commands.
     */
    NSFW("re_icon_nsfw",1019221923466584166L, false, "category.nsfw"),
    /**
     * Category used for admin Commands.
     */
    HIDDEN("re_icon_hidden",1019221957817933865L, false, "category.hidden");

    /**
     * The Name of the Icon.
     */
    private final String icon;

    /**
     * The ID of the Icon.
     */
    private final long iconId;

    /**
     * Value for knowledge about the Emoji being animated or not.
     */
    private final boolean iconAnimated;

    /**
     * A short description of the Categories.
     */
    private final String description;

    /**
     * Constructor.
     * @param icon the Icon Name.
     * @param iconId the Icon ID.
     * @param iconAnimated the info if the Icon is animated.
     * @param description a short description.
     */
    Category(String icon, long iconId, boolean iconAnimated, String description) {
        this.icon = icon;
        this.iconId = iconId;
        this.iconAnimated = iconAnimated;
        this.description = description;
    }

    /**
     * The Icon Name for the Category.
     * @return the Name.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * The Icon ID for the Category.
     * @return the Icon ID.
     */
    public long getIconId() {
        return iconId;
    }

    /**
     * The Information if the Icon is animated.
     * @return true, if yes | false, if not.
     */
    public boolean isIconAnimated() {
        return iconAnimated;
    }

    /**
     * Short description of the Category.
     * @return the short description.
     */
    public String getDescription() {
        return description;
    }
}
