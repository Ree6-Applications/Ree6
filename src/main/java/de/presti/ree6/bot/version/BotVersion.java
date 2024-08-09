package de.presti.ree6.bot.version;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class with every Version.
 */
@Getter
@AllArgsConstructor
public enum BotVersion {

    /**
     * Version for Development tests.
     */
    DEVELOPMENT("Dev","bot.tokens.dev"),
    /**
     * Version for a not yet fully stable release.
     */
    BETA("Beta", "bot.tokens.beta"),
    /**
     * Version for a stable release.
     */
    RELEASE("Release", "bot.tokens.release");

    /**
     * The name of the Version.
     */
    final String name;

    /**
     * The Token-Path in the config file.
     */
    final String tokenPath;

    @Override
    public String toString() {
        return name;
    }
}
