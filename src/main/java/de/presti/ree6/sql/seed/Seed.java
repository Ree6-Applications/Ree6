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
        public int MAJOR = 0;

        /**
         * The Minor version of the Seed.
         */
        public int MINOR = 0;

        /**
         * The Patch version of the Seed.
         */
        public int PATCH = 0;

        /**
         * Constructor for the SeedVersion.
         * @param MAJOR The Major version of the Seed.
         * @param MINOR The Minor version of the Seed.
         * @param PATCH The Patch version of the Seed.
         */
        public SeedVersion(int MAJOR, int MINOR, int PATCH) {
            this.MAJOR = MAJOR;
            this.MINOR = MINOR;
            this.PATCH = PATCH;
        }
    }
}
