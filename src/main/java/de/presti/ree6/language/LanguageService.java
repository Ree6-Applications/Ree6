package de.presti.ree6.language;

import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Utility used to work with Languages.
 */
@Slf4j
public class LanguageService {

    /**
     * A Hashmap containing the locale as key and the YamlConfiguration as value.
     */
    public static final HashMap<DiscordLocale, Language> languageResources = new HashMap<>();

    /**
     * Called to download every Language file from the GitHub Repository.
     */
    public static void downloadLanguages() {
        try {
            RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.github.com/repos/Ree6-Applications/Ree6/contents/languages").build()).getAsJsonArray().forEach(jsonElement -> {
                String language = jsonElement.getAsJsonObject().get("name").getAsString().replace(".yml", "");
                String download = jsonElement.getAsJsonObject().get("download_url").getAsString();

                Path languageFile = Path.of("languages/", language + ".yml");

                if (Files.exists(languageFile)) {
                    log.info("Ignoring Language download: {}", language);
                    return;
                }

                if (!languageFile.toAbsolutePath().startsWith(Path.of("languages/").toAbsolutePath())) {
                    log.info("Ignoring Language download, since Path Traversal has been detected!");
                    return;
                }

                log.info("Downloading Language: {}", language);

                try (InputStream inputStream = RequestUtility.request(RequestUtility.Request.builder().url(download).build())) {
                    if (inputStream == null) return;

                    Files.copy(inputStream, languageFile);
                } catch (IOException exception) {
                    log.error("An error occurred while downloading the language file!", exception);
                }
            });
        } catch (Exception exception) {
            log.error("An error occurred while downloading the language files!", exception);
        }
    }

    /**
     * Called to load a Language from a YamlConfiguration.
     * @param discordLocale The DiscordLocale of the Language.
     * @return The Language.
     */
    public static @Nullable Language loadLanguageFromFile(@NotNull DiscordLocale discordLocale) {
        Path languageFile = Path.of("languages/", discordLocale.getLocale() + ".yml");
        if (Files.exists(languageFile)) {
            try {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(languageFile.toFile());
                Language language = new Language(yamlConfiguration);
                languageResources.put(discordLocale, language);
                return language;
            } catch (Exception exception) {
                log.error("Error while getting Language File!", exception);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Called to get a specific String from the Guild specific Language file.
     * @param commandEvent the CommandEvent.
     * @param key the key of the String.
     * @param parameter the parameter to replace.
     * @return the String.
     */
    public static @NotNull String getByEvent(@NotNull CommandEvent commandEvent, @NotNull String key, @Nullable Object... parameter) {
        return getByGuild(commandEvent.getGuild(), key, parameter);
    }

    /**
     * Called to get a specific String from the Language file.
     * @param guild The Guild to receive the locale from.
     * @param key The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull String getByGuild(Guild guild, @NotNull String key, @Nullable Object... parameter) {
        return getByGuild(guild != null ? guild.getIdLong() : -1, key, parameter);
    }

    /**
     * Called to get a specific String from the Language file.
     * @param guildId The Guild ID to receive the locale from.
     * @param key The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull String getByGuild(long guildId, @NotNull String key, @Nullable Object... parameter) {
        if (guildId == -1) {
            return getDefault(key, parameter);
        } else {
            return getByLocale(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(String.valueOf(guildId), "configuration_language").getStringValue(), key, parameter);
        }
    }

    /**
     * Called to get a specific String from the default Language file.
     * @param interaction The Interaction to receive the locale from.
     * @param key The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull String getByInteraction(Interaction interaction, @NotNull String key, @Nullable Object... parameter) {
        return getByLocale(interaction.getUserLocale(), key, parameter);
    }

    /**
     * Called to get a specific String from the default Language file.
     * @param key The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull String getDefault(@NotNull String key, @Nullable Object... parameter) {
        return getByLocale(DiscordLocale.ENGLISH_UK, key, parameter);
    }

    /**
     * Called to get a specific String from the Language file.
     * @param locale The locale of the Language file.
     * @param key The key of the String.
     * @param parameters The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull String getByLocale(@NotNull String locale, @NotNull String key, @Nullable Object... parameters) {
        return getByLocale(DiscordLocale.from(locale), key, parameters);
    }

    /**
     * Called to get a specific String from the Language file.
     * @param discordLocale The locale of the Language file.
     * @param key The key of the String.
     * @param parameters The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull String getByLocale(@NotNull DiscordLocale discordLocale, @NotNull String key, @Nullable Object... parameters) {
        Language language = languageResources.containsKey(discordLocale) ? languageResources.get(discordLocale) : loadLanguageFromFile(discordLocale);
        return language != null ? language.getResource(key, parameters) : "Missing language resource!";
    }
}
