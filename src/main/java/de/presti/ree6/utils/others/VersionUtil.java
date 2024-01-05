package de.presti.ree6.utils.others;

/**
 * Utility class for Version related stuff.
 */
public class VersionUtil {
    /**
     * Compare two versions that are based on the x.y.z format.
     *
     * @param versionA the base version.
     * @param versionB the version that should be tested against version A.
     * @return Returns none if version A is the same or less than version B, but if version A is higher, you receive the difference.
     */
    public static VersionType compareVersion(String versionA, String versionB) {
        if (versionA == null) return VersionType.NONE;
        if (versionB == null) return VersionType.MAYOR;

        if (versionA.equals(versionB)) return VersionType.NONE;

        String[] split = versionA.split("\\.");

        if (split.length != 3) return VersionType.NONE;

        int mayor = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        int patch = Integer.parseInt(split[2]);

        String[] split2 = versionB.split("\\.");

        if (split2.length != 3) return VersionType.MAYOR;

        int otherMayor = Integer.parseInt(split2[0]);
        int otherMinor = Integer.parseInt(split2[1]);
        int otherPatch = Integer.parseInt(split2[2]);

        if (mayor > otherMayor) return VersionType.MAYOR;
        if (mayor == otherMayor && minor > otherMinor) return VersionType.MINOR;
        if (mayor == otherMayor && minor == otherMinor && patch > otherPatch) return VersionType.PATCH;
        return VersionType.NONE;
    }

    public enum VersionType {
        MAYOR,
        MINOR,
        PATCH,
        NONE
    }
}
