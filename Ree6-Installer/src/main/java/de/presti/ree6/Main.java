package de.presti.ree6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Main class.
 */
public class Main {

    /**
     * The Config.
     */
    private static final Config config = new Config();

    /**
     * Main method.
     * @param args the args.
     * @throws InterruptedException if the delay fails?
     */
    public static void main(String[] args) throws InterruptedException {
        if (getJavaVersion() < 17) {
            print(String.format("""
                    Looks like you are using a version below Java 17!
                    Ree6 has been developed base on Java 17 you wont be able to run it with %s!
                    You can still continue with installing everything!""", getJavaVersion()));
        }

        if (Arrays.stream(args).anyMatch(c -> c.equalsIgnoreCase("update"))) {
            print("Updating Ree6...");
            update();
            return;
        }

        if (Files.exists(Paths.get("config.yml"))) {
            print("We found a config.yml!\nDo you want to update Ree6 or fully configure it? (update/configure)");

            String input = getValue();
            switch (input) {
                case "update" -> {
                    print("Updating Ree6...");
                    update();
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
        config.init();

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
        print("Welcome to the setup System of Ree6!\nLets start by configuration the Config!\nPlease select one of these Database Types: MariaDB, SQLite, Postgres, H2, H2-Server");

        switch (getValueOrDefault("sqlite").toLowerCase()) {
            case "mariadb" -> setupGenericDatabase("mariadb", "MariaDB");

            case "sqlite" -> setupSQLite();

            case "h2" -> setupH2(false);

            case "h2-server" -> setupH2(true);

            case "postgres" -> setupGenericDatabase("postgresql", "Postgres");

            default -> {
                print("Unknown Database Typ!");
                Thread.sleep(500);
                setupStepOne();
            }
        }

        setupStepTwo();
    }

    /**
     * Setup the second step of the configuration.
     * @throws IOException if something went wrong.
     */
    public static void setupStepTwo() throws IOException {
        clear();
        print("The Database configuration looks fine!\nLets continue with our API-Keys!\nKeys marked with * are required!");

        print("Enter your Discord Bot-Token (*)");
        config.getConfiguration().set("bot.tokens.release", getValueOrDefaultHidden(""));

        print("Do you want to continue configuration not mandatory configs?\nEnter yes if you want to continue if not enter no!");

        if (getValueOrDefault("no").toLowerCase().startsWith("y")) {

            print("Enter the Heartbeat-Url (NONE)");
            config.getConfiguration().set("heartbeat.url", getValueOrDefault(""));

            print("Enter the Heartbeat-Interval (NONE)");
            config.getConfiguration().set("heartbeat.url", getValueOrDefault(""));

            print("Enter your Dagpi.xyz-Key (NONE)");
            config.getConfiguration().set("dagpi.apitoken", getValueOrDefault(""));

            print("Enter your AmariBot-Key (NONE)");
            config.getConfiguration().set("amari.apitoken", getValueOrDefault(""));

            print("Enter your OpenAI API-Token (NONE)");
            config.getConfiguration().set("openai.apiToken", getValueOrDefault(""));

            print("Enter your OpenAI API-Url (NONE)");
            config.getConfiguration().set("openai.apiUrl", getValueOrDefault(""));

            print("Enter your OpenAI AI-Model (NONE)");
            config.getConfiguration().set("openai.model", getValueOrDefault(""));

            print("Enter your Sentry DSN (NONE)");
            config.getConfiguration().set("sentry.dsn", getValueOrDefault(""));

            print("Enter your Spotify Client Id (NONE)");
            config.getConfiguration().set("spotify.client.id", getValueOrDefault(""));

            print("Enter your Spotify Client Secret (NONE)");
            config.getConfiguration().set("spotify.client.secret", getValueOrDefaultHidden(""));

            print("Enter your Twitch Client Id (NONE)");
            config.getConfiguration().set("twitch.client.id", getValueOrDefault(""));

            print("Enter your Twitch Client Secret (NONE)");
            config.getConfiguration().set("twitch.client.secret", getValueOrDefaultHidden(""));

            print("Enter your Twitter Bearer Key (NONE)");
            config.getConfiguration().set("twitter.bearer", getValueOrDefault(""));

            print("Enter your Reddit Client Id (NONE)");
            config.getConfiguration().set("reddit.client.id", getValueOrDefault(""));

            print("Enter your Reddit Client Secret (NONE)");
            config.getConfiguration().set("reddit.client.secret", getValueOrDefaultHidden(""));

            print("Enter your Instagram Account Name (NONE)");
            config.getConfiguration().set("instagram.username", getValueOrDefault(""));

            print("Enter your Instagram Account password (NONE)");
            config.getConfiguration().set("instagram.password", getValueOrDefaultHidden(""));
        }

        config.getConfiguration().save();

        print("Great, we finished setting up everything!\nDo you want me to download the latest version?\nEnter yes if you want to continue if not enter no!");
        if (getValueOrDefault("no").toLowerCase().startsWith("y")) {
            print("Understood!\nDownloading latest version...");
            update();
        }
    }

    /**
     * Update Ree6 to the latest version.
     */
    public static void update() {
        print("Requesting version list from Github...");
        JSONArray jsonObject = new JSONArray(RequestUtility.request("https://api.github.com/repos/Ree6-Applications/Ree6/releases"));

        JSONObject jsonObject1 = jsonObject.getJSONObject(0);

        JSONArray assets = jsonObject1.getJSONArray("assets");

        String tagName = jsonObject1.getString("tag_name");

        print("Found " + jsonObject.length() + " versions!\nSearching for latest version...");

        for (Object o : assets) {
            JSONObject asset = (JSONObject) o;
            if (asset.getString("name").contains("-jar-with-dependencies.jar")) {
                String downloadUrl = asset.getString("browser_download_url");

                try {
                    print("Found Ree6 version " + tagName + "!\nDownloading it...");
                    InputStream in = new URL(downloadUrl).openStream();
                    Files.copy(in, Paths.get("Ree6.jar"), StandardCopyOption.REPLACE_EXISTING);
                    print("We downloaded the latest Ree6 version! (" + tagName + ")");
                } catch (Exception exception) {
                    print("We could not download the latest Ree6 version!\nManually download it over " + downloadUrl);
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
        config.getConfiguration().set("hikari.misc.storage", typ);

        print("You selected " + displayName + "!\nLets start by setting up the connection between Ree6 and " + displayName + "!\nWhat is the Username that you want to use? (root)");
        String name = getValueOrDefault("root");
        config.getConfiguration().set("hikari.sql.name", name);

        print("What is the host address? (localhost)");
        String host = getValueOrDefault("localhost");
        config.getConfiguration().set("hikari.sql.host", host);

        print("What is the host port? (3306)");
        String port = getValueOrDefault("3306");
        config.getConfiguration().set("hikari.sql.port", Integer.parseInt(port));

        print("What is the Database name? (root)");
        String databaseName = getValueOrDefault("root");
        config.getConfiguration().set("hikari.sql.db", databaseName);


        print("What is the User password? (NONE)");
        String password = getValueOrDefaultHidden("");
        config.getConfiguration().set("hikari.sql.pw", password);

        config.getConfiguration().save();
    }

    /**
     * Setup SQLite.
     * @throws IOException if something went wrong.
     */
    public static void setupSQLite() throws IOException {
        clear();
        config.getConfiguration().set("hikari.misc.storage", "sqlite");

        print("You selected SQLite!\nYou dont need to set up anything for this one!");

        config.getConfiguration().save();
    }

    /**
     * Setup H2.
     * @throws IOException if something went wrong.
     */
    public static void setupH2(boolean server) throws IOException {
        clear();
        config.getConfiguration().set("hikari.misc.storage", server ? "h2-server" : "h2");

        if (server) {
            print("Do you want to run the H2 server?\nEnter yes if you want or no If you dont.");
            String input = getValue();
            if (input.equalsIgnoreCase("yes")) {
                config.getConfiguration().set("hikari.misc.createEmbeddedServer", true);

                print("Done!\nRee6 will now start a H2 embedded Server when you start it!\nThe port to connect is: 9092!");
            } else {
                config.getConfiguration().set("hikari.misc.createEmbeddedServer", false);

                setupGenericDatabase("h2-server", "H2-Server");

                print("Done!\nRee6 will now connect to the H2 server when you start it!");
            }
        } else {
            config.getConfiguration().set("hikari.misc.createEmbeddedServer", false);
            print("You selected H2!\nYou dont need to set up anything for this one!");
        }

        config.getConfiguration().save();
    }

    /**
     * Get a value from the console.
     * @return the value.
     */
    public static String getValue() {
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
        String value = String.valueOf(System.console().readPassword());

        return value.isBlank() ? defaultValue : value;
    }
}