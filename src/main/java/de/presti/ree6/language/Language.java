package de.presti.ree6.language;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.HashMap;

/**
 * Class used to represent a Language.
 */
@Slf4j
public class Language {

    /**
     * The Locale-Tag of the Language.
     */
    private final String locale;

    /**
     * The Name of the Language.
     */
    private final String name;

    /**
     * The Author of the Language-File.
     */
    private final String author;

    /**
     * The corresponding Ree6 Version.
     */
    private final String version;

    /**
     * The DiscordLocale of the Language.
     */
    private final DiscordLocale discordLocale;

    /**
     * All entries of the Language.
     */
    final HashMap<String, String> resources = new HashMap<>();

    /**
     * Constructor used to create a Language.
     * @param yamlConfiguration The YamlConfiguration of the Language.
     */
    public Language(@NotNull YamlConfiguration yamlConfiguration) {
        this.locale = yamlConfiguration.getString("language.locale");
        this.name = yamlConfiguration.getString("language.name");
        this.author = yamlConfiguration.getString("language.author");
        this.version = yamlConfiguration.getString("language.version");

        yamlConfiguration.getKeys(true).forEach(key -> {
            if (key.startsWith("language.")) return;

            resources.putIfAbsent(key, yamlConfiguration.getString(key));
        });

        discordLocale = DiscordLocale.from(locale);
    }

    /**
     * Constructor used to create a Language.
     * @param locale The Locale-Tag of the Language.
     * @param name The Name of the Language.
     * @param author The Author of the Language-File.
     * @param version The corresponding Ree6 Version.
     * @param resources All entries of the Language.
     */
    public Language(@NotNull String locale, @NotNull String name, @NotNull String author, @NotNull String version, @NotNull HashMap<String, String> resources) {
        this.locale = locale;
        this.name = name;
        this.author = author;
        this.version = version;
        this.resources.putAll(resources);
        discordLocale = DiscordLocale.from(locale);
    }

    /**
     * Called to get the Locale-Tag of the Language.
     * @return The Locale-Tag.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Called to get the Name of the Language.
     * @return The Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Called to get the Author of the Language-File.
     * @return The Author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Called to get the corresponding Ree6 Version.
     * @return The Version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Called to get the DiscordLocale of the Language.
     * @return The DiscordLocale.
     */
    public DiscordLocale getDiscordLocale() {
        return discordLocale;
    }

    /**
     * Called to get the entry of the Language.
     * @param key The key of the entry.
     * @param parameter The parameter that should be used to replace placeholders.
     * @return The entry.
     */
    public String getResource(@NotNull String key, @Nullable Object... parameter) {
        if (!resources.containsKey(key)) {
            log.info("Missing Language-Entry: {}", key);

            SentryEvent sentryEvent = new SentryEvent();
            Message message = new Message();
            message.setMessage("Missing Language-Entry: " + key);
            sentryEvent.setMessage(message);
            sentryEvent.setLevel(SentryLevel.ERROR);
            Sentry.captureEvent(sentryEvent);

            return "Missing language resource!";
        }
        try {
            return String.format(resources.get(key), parameter);
        } catch (Exception e) {
            log.error("Error while formatting language resource! (" + key + ")", e);

            SentryEvent sentryEvent = new SentryEvent();
            Message message = new Message();
            message.setMessage("Error while formatting language resource! (" + key + ")");
            sentryEvent.setMessage(message);
            sentryEvent.setThrowable(e.getCause());
            sentryEvent.setLevel(SentryLevel.FATAL);
            Sentry.captureEvent(sentryEvent);

            return "Error while formatting language resource!";
        }
    }

    /**
     * Compare the current Language version with another Language.
     * @param language The Language to compare with.
     * @return The result of the comparison. True, if it should update | False, if it should not be updated.
     */
    public boolean compareVersion(Language language) {
        if (language == null) return false;
        if (language.getVersion() == null) return false;
        if (version == null) return true;
        if (language.getVersion().equals(version)) return false;

        String[] split = version.split("\\.");

        int mayor = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        int patch = Integer.parseInt(split[2]);

        String[] split2 = language.getVersion().split("\\.");
        int otherMayor = Integer.parseInt(split2[0]);
        int otherMinor = Integer.parseInt(split2[1]);
        int otherPatch = Integer.parseInt(split2[2]);

        if (otherMayor > mayor) return true;
        if (otherMayor == mayor && otherMinor > minor) return true;
        return otherMayor == mayor && otherMinor == minor && otherPatch > patch;
    }
}
