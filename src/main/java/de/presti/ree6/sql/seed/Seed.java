package de.presti.ree6.sql.seed;

import de.presti.ree6.sql.SQLConnector;

/**
 * Seed is used to seed the database with data.
 */
public abstract class Seed {

    /**
     * The Version of this Seed.
     */
    private SeedVersion seedVersion;

    /**
     * Seed executor.
     *
     * @param sqlConnector The SQLConnector.
     */
    public abstract void run(SQLConnector sqlConnector);

    /**
     * Get the Version of this Seed.
     *
     * @return The Version of this Seed.
     */
    public SeedVersion getSeedVersion() {
        return seedVersion;
    }

    /**
     * SeedVersion class.
     *
     * @param majorVersion The Major version of the Seed.
     * @param minorVersion The Minor version of the Seed.
     * @param patchVersion The Patch version of the Seed.
     */
    public record SeedVersion(int majorVersion, int minorVersion, int patchVersion) {

        /**
         * Convert the SeedVersion to a String.
         *
         * @return The String representation of the SeedVersion.
         */
        @Override
        public String toString() {
            return majorVersion + "." + minorVersion + "." + patchVersion;
        }
    }
}
