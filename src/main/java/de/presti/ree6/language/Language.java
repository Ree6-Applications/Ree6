package de.presti.ree6.language;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.HashMap;

public class Language {

    private final String locale;
    private final String name;
    private final String author;
    private final String version;
    private final DiscordLocale discordLocale;
    final HashMap<String, String> resources = new HashMap<>();

    public Language(@NotNull YamlConfiguration yamlConfiguration) {
        this.locale = yamlConfiguration.getString("language.locale");
        this.name = yamlConfiguration.getString("language.name");
        this.author = yamlConfiguration.getString("language.author");
        this.version = yamlConfiguration.getString("language.version");

        yamlConfiguration.getKeys(true).forEach(key -> {
            if (key.startsWith("language.")) return;

            resources.put(key, yamlConfiguration.getString(key));
        });

        discordLocale = DiscordLocale.from(locale);
    }

    public Language(@NotNull String locale, @NotNull String name, @NotNull String author, @NotNull String version, @NotNull HashMap<String, String> resources) {
        this.locale = locale;
        this.name = name;
        this.author = author;
        this.version = version;
        this.resources.putAll(resources);
        discordLocale = DiscordLocale.from(locale);
    }

    public String getLocale() {
        return locale;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public DiscordLocale getDiscordLocale() {
        return discordLocale;
    }

    public String getResource(@NotNull String key, @Nullable Object... parameter) {
        if (!resources.containsKey(key)) return "Missing language resource!";
        return String.format(resources.get(key), parameter);
    }
}
