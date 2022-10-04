package de.presti.ree6.utils.localization;
;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import org.checkerframework.checker.units.qual.N;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a {@link Map} with all supported {@link Language}s and their values. <br>
 * Also it keeps all the values in the map up to date.
 * * @see #get(Interaction)
 * @see #get(Language)
 */
public class LocalizationService {

    private final static String fileName = "locales.csv";
    private final static String CSV_SPLITTER_PATTERN = ",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
    private final static Logger log = Main.getInstance().getLogger();

    /**
     * A map containing all supported {@link Language}s and their tag plus actual string.
     */
    private final static Map<Language, Map<String, String>> LANGUAGES = new HashMap<>();

    /**
     * Initializes the localization. <br>
     * Downloads the localization file and updates the map.
     */
    public LocalizationService() {
        try {
            download();
            initLanguagesMap();
        } catch (IOException e) {
            log.error("Failed to initialize localization system.");
        }
    }

    /**
     * Downloads the locale .csv file from the Google spreadsheet and saves it to the localization folder.
     *
     * @throws IOException if the file couldn't be downloaded.
     */
    private synchronized void download() throws IOException {
        log.debug("Downloading {}", fileName);
        try (BufferedInputStream in = new BufferedInputStream(new URL(Main.getInstance().getConfig().getConfiguration()
                .get("bot.misc.localizationCsvURL").toString()).openStream())) {
            FileOutputStream out = new FileOutputStream(fileName);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                log.debug("Read {} bytes", bytesRead);
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            log.info("Successfully downloaded or updated {}.", fileName);
        }
    }

    /**
     * Reads the locale File {@link LocalizationService#fileName} and parses the content to the {@link LocalizationService#LANGUAGES} map.
     */
    private void initLanguagesMap() {
        for (Language language : Language.values()) {
            LANGUAGES.put(language, readColumnForLanguage(language));
        }
    }

    /**
     * Reads the column for the given language.
     *
     * @param language the language to read the column for.
     * @throws UncheckedIOException if there was an error while reading the file.
     * @return the map containing all keys and values for the given language.
     */
    private @Nonnull Map<String, String> readColumnForLanguage(@Nonnull Language language) {
        log.debug("Reading column for language {}", language);
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            Map<String, String> languageMap = new HashMap<>();
            reader.lines().skip(1).forEach(line -> {
                String[] split = line.split(CSV_SPLITTER_PATTERN);
                if (split.length == 0) {
                    log.debug("Empty line, skipping.");
                    return;
                }
                languageMap.put(split[0], split[language.getColumnIndex()]);
            });
            return languageMap;
        } catch (IOException e) {
            log.error("Failed to the column: {} for the language: {}.", language.getColumnIndex(), language);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets you access to the {@link Map} containing all values for the given language.
     *
     * @param event the {@link CommandEvent} which gives us the {@link Interaction}.
     * @return a {@link Map} that contains the keys and values of the specific language or the {@link Map}
     * for the {@link Language#ENGLISH} if the {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent}
     * was null or the language is not supported by this system.
     */
    public static Map<String, String> get(@Nonnull CommandEvent event) {
        if (event.getSlashCommandInteractionEvent() != null) {
            return get(event.getSlashCommandInteractionEvent());
        } else {
            return get(Language.ENGLISH);
        }
    }

    /**
     * Gets you access to the {@link Map} containing all values for the given language.
     *
     * @param event the event that gives us the {@link net.dv8tion.jda.api.interactions.DiscordLocale} via {@link GenericCommandInteractionEvent#getUserLocale()}.
     * @return a {@link Map} that contains the keys and values of the specific language.
     */
    public static Map<String, String> get(@Nonnull Interaction event) {
        return get(Language.from(event.getUserLocale()));
    }

    /**
     * Gets you access to the {@link Map} containing all values for the given language.
     *
     * @param language the {@link Language} you want the {@link Map} from.
     * @return a {@link Map} that contains the keys and values of the specific language.
     */
    public static Map<String, String> get(@Nonnull Language language) {
        return LANGUAGES.get(language);
    }
}