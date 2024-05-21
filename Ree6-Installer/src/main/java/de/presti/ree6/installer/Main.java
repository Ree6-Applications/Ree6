package de.presti.ree6.installer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main class.
 */
public class Main {

    /**
     * The Config.
     */
    private static final Config CONFIG = new Config();

    /**
     * The Scanner.
     */
    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Main method.
     * @param args the args.
     * @throws InterruptedException if the delay fails?
     */
    public static void main(String[] args) throws InterruptedException {
        if (getJavaVersion() < 17) {
            print(String.format("""
                    Looks like you are using a version below Java 17!
                    Ree6 has been developed on Java 17 you wont be able to run it with %s!
                    You can still continue with installing everything!""", getJavaVersion()));
        }

        Optional<String> updateArgument = Arrays.stream(args).filter(c -> c.equalsIgnoreCase("update"))
                .findFirst();

        if (updateArgument.isPresent()) {
            String version = updateArgument.get().substring("update".length()).trim();
            version = version.isBlank() ? "latest" : version;
            print("Updating Ree6...");
            update(version);
            return;
        }

        if (Files.exists(Paths.get("config.yml"))) {
            print("We found a config.yml!\nDo you want to update Ree6 or fully configure it? (update/configure)");

            String input = getValue();
            switch (input) {
                case "update" -> {
                    print("Updating Ree6...");
                    update("latest");
                }
                case "configure" -> {
                    print("Configuring Ree6...");
                    config();
                }
                default -> {
                    print("Invalid input!");
                    System.exit(0);
                }
            }
        } else {
            config();
        }
    }

    /**
     * Returns the Java version as an int value.
     *
     * @return the Java version as an int value (8, 9, etc.)
     */
    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Allow these formats:
        // 1.8.0_72-ea
        // 9-ea
        // 9
        // 9.0.1
        int dotPos = version.indexOf('.');
        int dashPos = version.indexOf('-');
        return Integer.parseInt(version.substring(0,
                dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : 1));
    }

    /**
     * Configure Ree6.
     * @throws InterruptedException if the delay fails?
     */
    public static void config() throws InterruptedException {
        CONFIG.init();

        try {
            setupStepOne();
        } catch (Exception exception) {
            exception.printStackTrace();
            print("Error, will restart!");
            Thread.sleep(1000);
            config();
        }
    }

    /**
     * Print something to the console.
     * @param print the thing to print.
     */
    public static void print(String print) {
        System.out.println();
        System.out.println(print);
    }

    /**
     * Clear the console.
     */
    public static void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException ignore) {
        }
    }

    /**
     * Set up the first step of the configuration.
     * @throws InterruptedException if the delay fails?
     * @throws IOException If something went wrong.
     */
    public static void setupStepOne() throws InterruptedException, IOException {
        clear();
        print("Welcome to the Ree6-Installer!\nLets start with the configuration of the installation!\nPlease select one of the supported Databases: MariaDB, SQLite, Postgres, H2, H2-Server");

        switch (getValueOrDefault("sqlite").toLowerCase()) {
            case "mariadb" -> setupGenericDatabase("mariadb", "MariaDB");

            case "sqlite" -> setupSQLite();

            case "h2" -> setupH2(false);

            case "h2-server" -> setupH2(true);

            case "postgres" -> setupGenericDatabase("postgresql", "Postgres");

            default -> {
                print("Unsupported Database!");
                Thread.sleep(500);
                setupStepOne();
            }
        }

        setupStepTwo();
    }

    /**
     * Set up the second step of the configuration.
     * @throws IOException if something went wrong.
     */
    public static void setupStepTwo() throws IOException {
        clear();
        print("The database configuration should be done!\nLets continue with setting other parameters!\nKeys marked with * are required!");

        print("Enter your Discord Bot-Token (*)");
        CONFIG.getConfiguration().set("bot.tokens.release", getValueOrDefaultHidden(""));

        print("Do you wish to configure the none mandatory parameters? (y/n)");

        if (getValueOrDefault("no").toLowerCase().startsWith("y")) {

            //region Heartbeat
            print("Enter the Heartbeat-Url (NONE)");
            CONFIG.getConfiguration().set("heartbeat.url", getValueOrDefault("none"));

            print("Enter the Heartbeat-Interval (NONE)");
            CONFIG.getConfiguration().set("heartbeat.url", getValueOrDefault(""));

            //endregion

            print("Enter your Dagpi.xyz-Key (NONE)");
            CONFIG.getConfiguration().set("dagpi.apitoken", getValueOrDefault(""));

            print("Enter your AmariBot-Key (NONE)");
            CONFIG.getConfiguration().set("amari.apitoken", getValueOrDefault(""));

            //region OpenAI

            print("Enter your OpenAI API-Token (NONE)");
            CONFIG.getConfiguration().set("openai.apiToken", getValueOrDefault(""));

            print("Enter your OpenAI API-Url (NONE)");
            CONFIG.getConfiguration().set("openai.apiUrl", getValueOrDefault("https://api.openai.com/v1/chat/completions"));

            print("Enter your OpenAI AI-Model (NONE)");
            CONFIG.getConfiguration().set("openai.model", getValueOrDefault("gpt-3.5-turbo-0301"));

            //endregion

            print("Enter your Sentry DSN (NONE)");
            CONFIG.getConfiguration().set("sentry.dsn", getValueOrDefault(""));

            //region Spotify

            print("Enter your Spotify Client Id (NONE)");
            CONFIG.getConfiguration().set("spotify.client.id", getValueOrDefault(""));

            print("Enter your Spotify Client Secret (NONE)");
            CONFIG.getConfiguration().set("spotify.client.secret", getValueOrDefaultHidden(""));

            //endregion

            //region Twitch

            print("Enter your Twitch Client Id (NONE)");
            CONFIG.getConfiguration().set("twitch.client.id", getValueOrDefault(""));

            print("Enter your Twitch Client Secret (NONE)");
            CONFIG.getConfiguration().set("twitch.client.secret", getValueOrDefaultHidden(""));

            //endregion

            print("Enter your Twitter Bearer Key (NONE)");
            CONFIG.getConfiguration().set("twitter.bearer", getValueOrDefault(""));

            //region Reddit

            print("Enter your Reddit Client Id (NONE)");
            CONFIG.getConfiguration().set("reddit.client.id", getValueOrDefault(""));

            print("Enter your Reddit Client Secret (NONE)");
            CONFIG.getConfiguration().set("reddit.client.secret", getValueOrDefaultHidden(""));

            //endregion

            //region Instagram

            print("Enter your Instagram Account Name (NONE)");
            CONFIG.getConfiguration().set("instagram.username", getValueOrDefault(""));

            print("Enter your Instagram Account password (NONE)");
            CONFIG.getConfiguration().set("instagram.password", getValueOrDefaultHidden(""));

            //endregion
        }

        CONFIG.getConfiguration().save();

        print("The configuration has been completed!\nDo you want me to download the latest version? (y/n)");
        if (getValueOrDefault("no").toLowerCase().startsWith("y")) {
            print("Understood!\nDownloading latest version...");
            update("latest");
        }
    }

    /**
     * Update Ree6 to the latest version.
     */
    public static void update(String version) {
        print("Requesting version list from Github...");
        JSONArray releaseArray = new JSONArray(RequestUtility.request("https://api.github.com/repos/Ree6-Applications/Ree6/releases"));

        JSONObject selectedVersion = null;

        if (!version.equalsIgnoreCase("latest")) {
            for (Object o : releaseArray) {
                JSONObject release = (JSONObject) o;
                if (release.getString("tag_name").equalsIgnoreCase(version)) {
                    selectedVersion = release;
                    break;
                }
            }
        } else {
            selectedVersion = releaseArray.getJSONObject(0);
        }

        if (selectedVersion == null) {
            print("Could not find version: " + version + "!\nPlease check the version and try again!");
            return;
        }

        JSONArray assets = selectedVersion.getJSONArray("assets");

        String tagName = selectedVersion.getString("tag_name");

        print("Found " + releaseArray.length() + " versions!\nSearching for latest version...");

        for (Object o : assets) {
            JSONObject asset = (JSONObject) o;
            if (asset.getString("name").contains("-jar-with-dependencies.jar")) {
                String downloadUrl = asset.getString("browser_download_url");

                try {
                    print("Found Ree6 version " + tagName + "!\nDownloading it...");
                    InputStream in = new URL(downloadUrl).openStream();
                    Files.copy(in, Paths.get("Ree6.jar"), StandardCopyOption.REPLACE_EXISTING);
                    print("Download completed!");
                } catch (Exception exception) {
                    print("We could not download the required Ree6 version!\nManually download it over " + downloadUrl);
                    exception.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * Setup a generic database.
     * @param typ the typ of the database.
     * @param displayName the display name of the database.
     * @throws IOException if something went wrong.
     */
    public static void setupGenericDatabase(String typ, String displayName) throws IOException {
        clear();
        CONFIG.getConfiguration().set("hikari.misc.storage", typ);

        print("You selected " + displayName + "!\nLets start by setting up the connection between Ree6 and " + displayName + "!\nWhat is the username that you want to use? (root)");
        String name = getValueOrDefault("root");
        CONFIG.getConfiguration().set("hikari.sql.name", name);

        print("What is the host address? (localhost)");
        String host = getValueOrDefault("localhost");
        CONFIG.getConfiguration().set("hikari.sql.host", host);

        print("What is the host port? (3306)");
        String port = getValueOrDefault("3306");
        CONFIG.getConfiguration().set("hikari.sql.port", Integer.parseInt(port));

        print("What is the Database name? (root)");
        String databaseName = getValueOrDefault("root");
        CONFIG.getConfiguration().set("hikari.sql.db", databaseName);


        print("What is the User password? (NONE)");
        String password = getValueOrDefaultHidden("");
        CONFIG.getConfiguration().set("hikari.sql.pw", password);

        CONFIG.getConfiguration().save();
    }

    /**
     * Setup SQLite.
     * @throws IOException if something went wrong.
     */
    public static void setupSQLite() throws IOException {
        clear();
        CONFIG.getConfiguration().set("hikari.misc.storage", "sqlite");

        print("You selected SQLite!\nYou dont need to set up anything for this one!");

        CONFIG.getConfiguration().save();
    }

    /**
     * Setup H2.
     * @throws IOException if something went wrong.
     */
    public static void setupH2(boolean server) throws IOException {
        clear();
        CONFIG.getConfiguration().set("hikari.misc.storage", server ? "h2-server" : "h2");

        if (server) {
            print("Do you want to run the H2 server?\nEnter yes if you want or no If you dont.");
            String input = getValue();
            if (input.equalsIgnoreCase("yes")) {
                CONFIG.getConfiguration().set("hikari.misc.createEmbeddedServer", true);

                print("Done!\nRee6 will now start a H2 embedded Server when you start it!\nThe port to connect to is: 9092!");
            } else {
                CONFIG.getConfiguration().set("hikari.misc.createEmbeddedServer", false);

                setupGenericDatabase("h2-server", "H2-Server");

                print("Done!\nRee6 will now connect to the H2 server when you start it!");
            }
        } else {
            CONFIG.getConfiguration().set("hikari.misc.createEmbeddedServer", false);
            print("You selected H2!\nYou dont need to set up anything for this one!");
        }

        CONFIG.getConfiguration().save();
    }

    /**
     * Get a value from the console.
     * @return the value.
     */
    public static String getValue() {
        if (System.console() == null) {
            return SCANNER.nextLine();
        }

        return System.console().readLine();
    }

    /**
     * Get a value from the console or a default value.
     * @param defaultValue the default value.
     * @return the value.
     */
    public static String getValueOrDefault(String defaultValue) {
        String value = getValue();

        return value.isBlank() ? defaultValue : value;
    }

    /**
     * Get a hidden value from the console or a default value.
     * @param defaultValue the default value.
     * @return the value.
     */
    public static String getValueOrDefaultHidden(String defaultValue) {
        String value = System.console() == null ? SCANNER.nextLine() : new String(System.console().readPassword());

        return value.isBlank() ? defaultValue : value;
    }
}