package de.presti.ree6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {

    private static final Config config = new Config();

    public static void main(String[] args) throws InterruptedException {
        if (getJavaVersion() < 17) {
            print("Looks like you are using a version below Java 17!\nRee6 has been developed base on Java 17 you wont be able to run it with " + getJavaVersion() + "!\nYou can still continue with installing everything!");
        }
        if (Files.exists(Paths.get("config.yml"))) {
            print("We found a config.yml!\nDo you want to update Ree6 or fully configure it? (update/configure)");

            String input = System.console().readLine();
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

    public static void print(String print) {
        System.out.println();
        System.out.println(print);
    }

    public static void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException ignore) {}
    }

    public static void setupStepOne() throws InterruptedException, IOException {
        clear();
        print("Welcome to the setup System of Ree6!\nLets start by configuration the Config!\nPlease select one of these Database Types: MariaDB, SQLLite, H2");

        switch (getValueOrDefault("sqllite").toLowerCase()) {
            case "mariadb" -> setupMariaDB();

            case "sqllite" -> setupSQLLite();

            default -> {
                print("Unknown Database Typ!");
                Thread.sleep(500);
                setupStepOne();
            }
        }

        setupStepTwo();
    }

    public static void setupStepTwo() throws IOException {
        clear();
        print("The Database configuration looks fine!\nLets continue with our API-Keys!\nKeys marked with * are required!");

        print("Enter your Dagpi.xyz-Key (NONE)");
        config.getConfiguration().set("dagpi.apitoken", getValueOrDefault(""));

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

        print("Enter your Twitter Consumer Id (NONE)");
        config.getConfiguration().set("twitter.consumer.key", getValueOrDefault(""));

        print("Enter your Twitter Consumer Secret (NONE)");
        config.getConfiguration().set("twitter.consumer.secret", getValueOrDefaultHidden(""));

        print("Enter your Twitter Access Id (NONE)");
        config.getConfiguration().set("twitter.access.key", getValueOrDefault(""));

        print("Enter your Twitter Access Secret (NONE)");
        config.getConfiguration().set("twitter.access.secret", getValueOrDefaultHidden(""));

        print("Enter your Reddit Client Id (NONE)");
        config.getConfiguration().set("reddit.client.id", getValueOrDefault(""));

        print("Enter your Reddit Client Secret (NONE)");
        config.getConfiguration().set("reddit.client.secret", getValueOrDefaultHidden(""));

        print("Enter your Instagram Account Name (NONE)");
        config.getConfiguration().set("instagram.username", getValueOrDefault(""));

        print("Enter your Instagram Account password (NONE)");
        config.getConfiguration().set("instagram.password", getValueOrDefaultHidden(""));

        print("Enter your Discord Bot-Token (*)");
        config.getConfiguration().set("bot.tokens.release", getValueOrDefaultHidden(""));

        config.getConfiguration().save();

        print("Great, we finished setting up everything!\nGive me a second to download the newest JAR!");
    }

    public static void update() {
        JSONArray jsonObject = new JSONArray(RequestUtility.request("https://api.github.com/repos/Ree6-Applications/Ree6/releases"));

        JSONObject jsonObject1 = jsonObject.getJSONObject(0);

        JSONArray assets = jsonObject1.getJSONArray("assets");

        String tagName = jsonObject1.getString("tag_name");

        for (Object o : assets) {
            JSONObject asset = (JSONObject) o;
            if (asset.getString("name").contains("-jar-with-dependencies.jar")) {
                String downloadUrl = asset.getString("browser_download_url");

                try {
                    InputStream in = new URL(downloadUrl).openStream();
                    Files.copy(in, Paths.get("Ree6.jar"), StandardCopyOption.REPLACE_EXISTING);
                    print("We downloaded the newest Ree6 version! (" + tagName + ")");
                } catch (Exception exception) {
                    print("We could not download the newest Ree6 version!\nManually download it over " + downloadUrl);
                    exception.printStackTrace();
                }
                break;
            }
        }
    }

    public static void setupMariaDB() throws IOException {
        clear();
        config.getConfiguration().set("hikari.misc.storage", "mariadb");

        print("You selected MariaDB!\nLets start by setting up the connection between Ree6 and MariaDB!\nWhat is the MariaDB-User that you want to use? (root)");
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

        print("What is the MariaDB-Password? (NONE)");
        String password = getValueOrDefaultHidden("");
        config.getConfiguration().set("hikari.sql.pw", password);

        config.getConfiguration().save();
    }

    public static void setupSQLLite() throws IOException {
        clear();
        config.getConfiguration().set("hikari.misc.storage", "sqlite");

        print("You selected SQLite!\nYou dont need to set up anything for this one!");

        config.getConfiguration().save();
    }

    public static String getValue() {
        return System.console().readLine();
    }

    public static String getValueOrDefault(String defaultValue) {
        String value = getValue();

        return value.isBlank() ? defaultValue : value;
    }

    public static String getValueOrDefaultHidden(String defaultValue) {
        String value = String.valueOf(System.console().readPassword());

        return value.isBlank() ? defaultValue : value;
    }
}