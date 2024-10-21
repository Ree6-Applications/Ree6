package de.presti.ree6.commands;

import lombok.Getter;

/**
 * Different Categories for the commands.
 */
@Getter
public enum Category {

    /**
     * Category used for information Commands.
     */
    INFO("re_icon_info", 1019221661070917632L, false, "category.info", true),
    /**
     * Category used for moderate Commands.
     */
    MOD("re_icon_mod", 1019221710932803624L, false, "category.moderation", true),
    /**
     * Category used for music Commands.
     */
    MUSIC("re_icon_music", 1019221781762023575L, false, "category.music", true),
    /**
     * Category used for fun Commands.
     */
    FUN("re_icon_fun", 1019221814670532628L, false, "category.fun", false),
    /**
     * Category used for level Commands.
     */
    LEVEL("re_icon_level", 1019221845972635809L, false, "category.level", true),
    /**
     * Category used for community Commands.
     */
    COMMUNITY("re_icon_community", 1019221884686057552L, false, "category.community", true),
    /**
     * Category used for economy Commands.
     */
    ECONOMY("re_icon_economy", 0L, false, "category.economy", true),
    /**
     * Category used for NSFW Commands.
     */
    NSFW("re_icon_nsfw", 1019221923466584166L, false, "category.nsfw", false),
    /**
     * Category used for admin Commands.
     */
    HIDDEN("re_icon_hidden", 1019221957817933865L, false, "category.hidden", true);

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
     * Value for knowledge about the Command being Guild only or not.
     */
    private final boolean guildOnly;

    /**
     * Constructor.
     *
     * @param icon         the Icon Name.
     * @param iconId       the Icon ID.
     * @param iconAnimated the info if the Icon is animated.
     * @param description  a short description.
     * @param guildOnly    the info if the Command is Guild only.
     */
    Category(String icon, long iconId, boolean iconAnimated, String description, boolean guildOnly) {
        this.icon = icon;
        this.iconId = iconId;
        this.iconAnimated = iconAnimated;
        this.description = description;
        this.guildOnly = guildOnly;
    }
}
