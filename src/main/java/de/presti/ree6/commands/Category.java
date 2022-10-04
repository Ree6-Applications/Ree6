package de.presti.ree6.commands;

import de.presti.ree6.utils.localization.Language;
import de.presti.ree6.utils.localization.LocalizationService;

/**
 * Different Categories for the commands.
 */
public enum Category {

    /**
     * Category used for informativ Commands.
     */
    INFO("re_icon_info",1019221661070917632L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_INFO")),
    /**
     * Category used for moderativ Commands.
     */
    MOD("re_icon_mod",1019221710932803624L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_MODERATION")),
    /**
     * Category used for music Commands.
     */
    MUSIC("re_icon_music",1019221781762023575L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_MUSIC")),
    /**
     * Category used for fun Commands.
     */
    FUN("re_icon_fun",1019221814670532628L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_FUN")),
    /**
     * Category used for level Commands.
     */
    LEVEL("re_icon_level",1019221845972635809L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_LEVEL")),
    /**
     * Category used for community Commands.
     */
    COMMUNITY("re_icon_community",1019221884686057552L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_COMMUNITY")),
    /**
     * Category used for NSFW Commands.
     */
    NSFW("re_icon_nsfw",1019221923466584166L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_NSFW")),
    /**
     * Category used for admin Commands.
     */
    HIDDEN("re_icon_hidden",1019221957817933865L, false, LocalizationService.get(Language.ENGLISH).get("CATEGORY_HIDDEN"));

    private final String icon;
    private final long iconId;
    private final boolean iconAnimated;
    private final String description;
    Category(String icon, long iconId, boolean iconAnimated, String description) {
        this.icon = icon;
        this.iconId = iconId;
        this.iconAnimated = iconAnimated;
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public long getIconId() {
        return iconId;
    }

    public boolean isIconAnimated() {
        return iconAnimated;
    }

    public String getDescription() {
        return description;
    }
}
