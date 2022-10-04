package de.presti.ree6.utils.localization;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;

public enum Language {

    ENGLISH(1, Locale.ENGLISH.toLanguageTag());

    private final int columnIndex;
    private final String languageTag;

    Language(int columnIndex, String languageTag) {
        this.columnIndex = columnIndex;
        this.languageTag = languageTag;
    }

    /**
     * Gets the {@link Locale#toLanguageTag()} from the current locale.
     * @return The tag as a {@link String}.
     */
    public String getLanguageTag() {
        return languageTag;
    }

    /**
     * Gets the column index from the csv file.
     * @return the index as an int.
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Checks by the language tag {@link Locale#toLanguageTag()} if the given language is supported.
     * @param languageTag the language tag to check.
     * @return true if the language is supported, false otherwise.
     */
    public static boolean isValid(String languageTag) {
        return Arrays.stream(Language.values()).anyMatch(l -> l.getLanguageTag().equals(languageTag));
    }

    /**
     * Gets you the {@link Language} from the given{@link Locale} or the standard Bot language configured in the BotConfig.toml file.
     * @param locale the {@link Locale} to get the language from.
     * @return the {@link Language} from the given {@link Locale}.
     * @see Language#from(DiscordLocale)
     */
    @Nonnull
    public static Language from(@Nonnull Locale locale) {
        return Arrays.stream(values()).filter(l -> l.getLanguageTag().equals(locale.toLanguageTag())).findFirst()
                .orElse(ENGLISH);
    }

    /**
     * Gets you the {@link Language} from the given {@link DiscordLocale}.
     * @param locale the {@link DiscordLocale} to get the language from.
     * @return the {@link Language} from the given {@link DiscordLocale}.
     * @see Language#from(Locale)
     */
    @Nonnull
    public static Language from(@Nonnull DiscordLocale locale) {
        return from(Locale.forLanguageTag(locale.getLocale()));
    }
}
