package de.presti.ree6.language;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.utils.data.RegExUtil;
import de.presti.ree6.utils.external.RequestUtility;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Utility used to work with Languages.
 */
@Slf4j
public class LanguageService {

    /**
     * A Hashmap containing the locale as a key and the YamlConfiguration as value.
     */
    public static final HashMap<DiscordLocale, Language> languageResources = new HashMap<>();

    /**
     * Called to load every Language file into memory.
     */
    public static void initializeLanguages() {
        Path languagePath = Path.of("languages");

        try {
            for (File file : Objects.requireNonNull(languagePath.toFile().listFiles())) {
                if (!file.getName().matches(RegExUtil.ALLOWED_LANGUAGE_FILES)) {
                    log.info("Skipping file {} because the file name is invalid!", file.getName());
                    continue;
                }

                try {
                    Language language = new Language(YamlConfiguration.loadConfiguration(file));
                    languageResources.putIfAbsent(language.getDiscordLocale(), language);
                } catch (Exception e) {
                    log.error("Couldn't load the language file {}!", file.getName(), e);
                }
            }
        } catch (Exception e) {
            log.error("Couldn't load the language files!", e);
        }
    }

    /**
     * Called to download every Language file from the GitHub Repository.
     */
    public static void downloadLanguages() {
        File languagePath = Path.of("languages/").toFile();
        if (!languagePath.exists()) {
            if (languagePath.mkdirs()) {
                log.info("Created the language folder!");
            } else {
                log.error("Couldn't create the language folder!");
            }
        }

        try {
            RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.github.com/repos/Ree6-Applications/Language/contents").build()).getAsJsonArray().forEach(jsonElement -> {
                String fileName = jsonElement.getAsJsonObject().get("name").getAsString();
                String language = fileName.replace(".yml", "");
                String download = jsonElement.getAsJsonObject().get("download_url").getAsString();

                if (!fileName.matches(RegExUtil.ALLOWED_LANGUAGE_FILES)) {
                    log.info("Skipping file {} because the file name is invalid!", fileName);
                    return;
                }


                Path languageFile = Path.of("languages/", language + ".yml");

                if (!languageFile.toAbsolutePath().startsWith(languagePath.toPath().toAbsolutePath())) {
                    log.info("Ignoring Language download, since Path Traversal has been detected!");
                    return;
                }

                log.info("Downloading Language: {}", language);

                try (InputStream inputStream = RequestUtility.request(RequestUtility.Request.builder().url(download).build())) {
                    if (inputStream == null) return;

                    String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

                    if (Files.exists(languageFile)) {

                        log.info("Version comparing: {}!", language);
                        YamlConfiguration newLanguageYaml = YamlConfiguration.loadConfigurationFromString(content);
                        Language newLanguage = new Language(newLanguageYaml);
                        Language oldLanguage = new Language(YamlConfiguration.loadConfiguration(languageFile.toFile()));
                        if (oldLanguage.compareVersion(newLanguage)) {
                            log.info("Language file {} is outdated! Will update!", language);
                            if (!languageFile.toFile().delete()) {
                                log.info("Failed to delete old Language file {}!", language);
                            }

                            // Not using YamlConfiguration#save, since that method breaks the whole file somehow? Unsure why?
                            Files.writeString(languageFile, content, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

                            log.info("Updated Language file {}!", language);

                            if (languageResources.remove(oldLanguage.getDiscordLocale()) != null) {
                                log.info("Removed old Language of {} from memory!", oldLanguage.getDiscordLocale().getLocale());
                            }
                        } else {
                            log.info("Language file {} is up to date!", language);
                        }
                    } else {
                        Files.writeString(languageFile, content, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        log.info("Downloaded Language: {}", language);
                    }
                } catch (IOException exception) {
                    log.error("An error occurred while downloading the language file!", exception);
                }
            });
        } catch (Exception exception) {
            log.error("An error occurred while downloading the language files!", exception);
        }

        initializeLanguages();
    }

    /**
     * Called to load a Language from a YamlConfiguration.
     *
     * @param discordLocale The DiscordLocale of the Language.
     * @return The Language.
     */
    public static @Nullable Language loadLanguageFromFile(@NotNull DiscordLocale discordLocale) {
        Path languageFile = Path.of("languages/", discordLocale.getLocale() + ".yml");
        if (Files.exists(languageFile)) {
            try {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(languageFile.toFile());
                Language language = new Language(yamlConfiguration);
                languageResources.putIfAbsent(discordLocale, language);
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
     *
     * @param commandEvent the CommandEvent.
     * @param key          the key of the String.
     * @param parameter    the parameter to replace.
     * @return the String.
     */
    public static @NotNull Mono<String> getByEvent(@NotNull CommandEvent commandEvent, @NotNull String key, @Nullable Object... parameter) {
        if (commandEvent.isSlashCommand()) {
            return getByInteraction(commandEvent.getInteractionHook().getInteraction(), key, parameter);
        } else {
            return getByGuild(commandEvent.getGuild(), key, parameter);
        }
    }

    /**
     * Called to get a specific String from the Guild specific Language file.
     *
     * @param commandEvent the GuildEvent.
     * @param key          the key of the String.
     * @param parameter    the parameter to replace.
     * @return the String.
     */
    public static @NotNull Mono<String> getByEvent(@NotNull GenericGuildEvent commandEvent, @NotNull String key, @Nullable Object... parameter) {
        return getByGuild(commandEvent.getGuild(), key, parameter);
    }

    /**
     * Called to get a specific String from the Language file.
     *
     * @param guild       The Guild to receive the locale from.
     * @param interaction The Interaction to receive the locale from.
     * @param key         The key of the String.
     * @param parameter   The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByGuildOrInteractionHook(Guild guild, InteractionHook interaction, @NotNull String key, @Nullable Object... parameter) {
        return getByGuildOrInteraction(guild, interaction != null ? interaction.getInteraction() : null, key, parameter);
    }


    /**
     * Called to get a specific String from the Language file.
     *
     * @param guild       The Guild to receive the locale from.
     * @param interaction The Interaction to receive the locale from.
     * @param key         The key of the String.
     * @param parameter   The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByGuildOrInteraction(Guild guild, Interaction interaction, @NotNull String key, @Nullable Object... parameter) {
        if (interaction != null) {
            return getByInteraction(interaction, key, parameter);
        } else {
            return getByGuild(guild, key, parameter);
        }
    }

    /**
     * Called to get a specific String from the Language file.
     *
     * @param guild     The Guild to receive the locale from.
     * @param key       The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByGuild(Guild guild, @NotNull String key, @Nullable Object... parameter) {
        return getByGuild(guild != null && !guild.isDetached() ? guild.getIdLong() : -1, key, parameter);
    }

    /**
     * Called to get a specific String from the Language file.
     *
     * @param guildId   The Guild ID to receive the locale from.
     * @param key       The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByGuild(long guildId, @NotNull String key, @Nullable Object... parameter) {
        if (guildId == -1) {
            return getDefault(key, parameter);
        }

        return SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, "configuration_language")
                .mapNotNull(setting -> getByLocale(setting.get().getStringValue(), key, parameter).block())
                .mapNotNull(resource -> SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, "chatprefix").
                        map(prefix -> resource.replace("{guild_prefix}", prefix.orElse(new Setting(-1, "chatprefix", "chatprefix", BotConfig.getDefaultPrefix()))
                                .getStringValue())).block());
    }

    /**
     * Called to get a specific String from the Language file.
     *
     * @param interaction The Interaction to receive the locale from.
     * @param key         The key of the String.
     * @param parameter   The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByInteraction(Interaction interaction, @NotNull String key, @Nullable Object... parameter) {
        if (!interaction.isFromGuild()) {
            return getByLocale(interaction.getUserLocale(), key, parameter);
        }

        return getByLocale(interaction.getUserLocale(), key, parameter).mapNotNull(resource -> {
            if (resource.contains("{guild_prefix}")) {
                return SQLSession.getSqlConnector().getSqlWorker().getSetting(interaction.getGuild().getIdLong(), "chatprefix")
                        .map(prefix -> resource.replace("{guild_prefix}", prefix.orElse(new Setting(-1, "chatprefix", "chatprefix", BotConfig.getDefaultPrefix()))
                                .getStringValue())).block();
            }

            return resource;
        });
    }

    /**
     * Called to get a specific String from the default Language file.
     *
     * @param key       The key of the String.
     * @param parameter The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getDefault(@NotNull String key, @Nullable Object... parameter) {
        return getByLocale(DiscordLocale.from(BotConfig.getDefaultLanguage()), key, parameter);
    }

    /**
     * Called to get a specific String from the Language file.
     *
     * @param locale     The locale of the Language file.
     * @param key        The key of the String.
     * @param parameters The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByLocale(@NotNull String locale, @NotNull String key, @Nullable Object... parameters) {
        return getByLocale(DiscordLocale.from(locale), key, parameters);
    }

    /**
     * Called to get a specific String from the Language file.
     *
     * @param discordLocale The locale of the Language file.
     * @param key           The key of the String.
     * @param parameters    The Parameters to replace placeholders in the String.
     * @return The String.
     */
    public static @NotNull Mono<String> getByLocale(@NotNull DiscordLocale discordLocale, @NotNull String key, @Nullable Object... parameters) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            if (discordLocale == DiscordLocale.UNKNOWN) return getDefault(key, parameters).block();

            Language language = languageResources.containsKey(discordLocale) ? languageResources.get(discordLocale) :
                    languageResources.get(DiscordLocale.from(BotConfig.getDefaultLanguage()));

            return language != null ? language.getResource(key, parameters) : "Missing language resource!";
        }));
    }

    /**
     * Check if given Locale has a translation for the key
     *
     * @param discordLocale The locale of the Language file.
     * @param key           The key of the String.
     * @return true, if the key is translated.
     */
    public static @NotNull Mono<Boolean> hasTranslation(@NotNull DiscordLocale discordLocale, @NotNull String key) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            if (discordLocale == DiscordLocale.UNKNOWN) return false;

            return languageResources.containsKey(discordLocale) && languageResources.get(discordLocale).resources.containsKey(key);
        }));
    }

    /**
     * Check if the default Locale has a translation for the key
     *
     * @param key The key of the String.
     * @return true, if the key is translated.
     */
    public static @NotNull Mono<Boolean> hasDefaultTranslation(@NotNull String key) {
        DiscordLocale discordLocale = DiscordLocale.from(BotConfig.getDefaultLanguage());
        return hasTranslation(discordLocale, key);
    }

    /**
     * Called to retrieve all supported Locals.
     *
     * @return The supported Locals.
     */
    public static Set<DiscordLocale> getSupported() {
        return languageResources.keySet();
    }
}
