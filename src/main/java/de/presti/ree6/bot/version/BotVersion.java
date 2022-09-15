package de.presti.ree6.bot.version;

/**
 * Class with every Version.
 */
public enum BotVersion {

    /**
     * Version for Development tests.
     */
    DEVELOPMENT_BUILD("bot.tokens.dev", 1, true),
    /**
     * Version for a not yet fully stable release.
     */
    BETA_BUILD("bot.tokens.beta", 5, false),
    /**
     * Version for a stable release.
     */
    RELEASE("bot.tokens.release", 10, false);

    /**
     * The Token-Path in the config file.
     */
    final String tokenPath;

    /**
     * The Shard-Count.
     */
    final int shards;

    /**
     * If the Bot version should activate the debug mode.
     */
    final boolean debug;

    /**
     * Constructor.
     *
     * @param tokenPath the Token-Path in the config file.
     * @param shards    the Shard-Count.
     * @param debug     if the Bot version should activate the debug mode.
     */
    BotVersion(String tokenPath, int shards, boolean debug) {
        this.tokenPath = tokenPath;
        this.shards = shards;
        this.debug = debug;
    }

    /**
     * Get the Token-Path in the config file.
     *
     * @return {@link String} as Token-Path.
     */
    public String getTokenPath() {
        return tokenPath;
    }

    /**
     * Get the Shard-Count.
     *
     * @return {@link Integer} as Shard-Count.
     */
    public int getShards() {
        return shards;
    }

    /**
     * If the Bot version should activate the debug mode.
     *
     * @return {@link Boolean} as debug mode.
     */
    public boolean isDebug() {
        return debug;
    }

}
