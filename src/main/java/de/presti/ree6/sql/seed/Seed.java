package de.presti.ree6.sql.seed;

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
     */
    public abstract void run();

    /**
     * SeedVersion class.
     */
    public static class SeedVersion {
        /**
         * The Major version of the Seed.
         */
        public int majorVersion;

        /**
         * The Minor version of the Seed.
         */
        public int minorVersion;

        /**
         * The Patch version of the Seed.
         */
        public int patchVersion;

        /**
         * Constructor for the SeedVersion.
         * @param majorVersion The Major version of the Seed.
         * @param minorVersion The Minor version of the Seed.
         * @param patchVersion The Patch version of the Seed.
         */
        public SeedVersion(int majorVersion, int minorVersion, int patchVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.patchVersion = patchVersion;
        }
    }
}
