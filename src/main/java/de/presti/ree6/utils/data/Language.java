package de.presti.ree6.utils.data;

import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.entities.Guild;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Utility used to work with Languages.
 */
public class Language {

    /**
     * A Hashmap containing the locale as key and the YamlConfiguration as value.
     */
    public static final HashMap<String, YamlConfiguration> languagesConfigurations = new HashMap<>();

    /**
     * Called to download every Language file from the GitHub Repository.
     */
    public static void downloadLanguages() {
        RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.github.com/repos/Ree6-Applications/Ree6/contents/languages").build()).getAsJsonArray().forEach(jsonElement -> {
            String language = jsonElement.getAsJsonObject().get("name").getAsString().replace(".yml", "");
            String download = jsonElement.getAsJsonObject().get("download_url").getAsString();
            Path languageFile = Path.of("languages/", language + ".yml");
            if (Files.exists(languageFile)) {
                Main.getInstance().getLogger().info("Ignoring Language download: " + language);
                return;
            }

            Main.getInstance().getLogger().info("Downloading Language: " + language);

            try (InputStream inputStream = RequestUtility.request(RequestUtility.Request.builder().url(download).build())) {
                if (inputStream == null) return;

                Files.copy(inputStream, languageFile);
            } catch (IOException exception) {
                Main.getInstance().getLogger().error("An error occurred while downloading the language file!", exception);
            }
        });
    }

    /**
     * Called to get a specific String from the Language file.
     * @param locale The locale of the Language file.
     * @param key The key of the String.
     * @return The String.
     */
    public static String getResource(String locale, String key) {
        YamlConfiguration yamlConfiguration = languagesConfigurations.get(locale);

        if (yamlConfiguration == null) {
            Path languageFile = Path.of("languages/", locale + ".yml");
            if (Files.exists(languageFile)) {
                try {
                    yamlConfiguration = YamlConfiguration.loadConfiguration(languageFile.toFile());
                    languagesConfigurations.put(locale, yamlConfiguration);
                    String resource = yamlConfiguration.getString(key);
                    return resource != null ? resource : "Missing language resource!";
                } catch (Exception exception) {
                    Main.getInstance().getLogger().error("Error while getting Resource!", exception);
                    return "Missing language resource!";
                }
            } else {
                return "Missing language resource!";
            }
        } else {
            String resource = yamlConfiguration.getString(key);
            return resource != null ? resource : "Missing language resource!";
        }
    }

    /**
     * Called to get a specific String from the Language file.
     * @param guildId The Guild ID to receive the locale from.
     * @param key The key of the String.
     * @return The String.
     */
    public static String getResource(long guildId, String key) {
        if (guildId == -1) {
            return getResource("en", key);
        } else {
            return getResource(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(String.valueOf(guildId), "configuration_language").getStringValue(), key);
        }
    }

    /**
     * Called to get a specific String from the Language file.
     * @param guild The Guild to receive the locale from.
     * @param key The key of the String.
     * @return The String.
     */
    public static String getResource(Guild guild, String key) {
        return getResource(guild != null ? guild.getIdLong() : -1, key);
    }
}
