package pw.aru.api.nekos4j;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NekosInfo {
    public static final String COMMIT = "@COMMIT_HASH@";
    public static final String VERSION_MAJOR = "@VERSION_MAJOR@";
    public static final String VERSION_MINOR = "@VERSION_MINOR@";
    public static final String VERSION_REVISION = "@VERSION_REVISION@";
    @SuppressWarnings("ConstantConditions")
    public static final String VERSION = VERSION_MAJOR.startsWith("@") ? "dev" : String.format("%s.%s.%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION);
}
