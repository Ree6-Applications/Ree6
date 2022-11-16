package de.presti.ree6;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class Main {

    private static final Config config = new Config();

    public static void main(String[] args) throws InterruptedException {
        config.init();

        try {
            setupStepOne();
        } catch (Exception exception) {
            exception.printStackTrace();
            print("Error, will restart!");
            Thread.sleep(1000);
            main(args);
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
            case "mariadb" -> {
                setupMariaDB();
            }

            case "sqllite" -> {
                setupSQLLite();
            }

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

        print("Enter your main YouTube-API-Key (NONE)");
        config.getConfiguration().set("youtube.api.key", getValueOrDefaultHidden(""));

        print("Enter your secondary YouTube-API-Key (NONE)");
        config.getConfiguration().set("youtube.api.key2", getValueOrDefaultHidden(""));

        print("Enter your Discord Bot-Token (*)");
        config.getConfiguration().set("bot.tokens.release", getValueOrDefaultHidden(""));

        config.getConfiguration().save();

        print("Great, we finished setting up everything!\nGive me a second to download the newest JAR!");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(new URL("https://api.github.com/repos/Ree6-Applications/Ree6/releases"));
        JsonNode latestReleaseNode = jsonNode.get(0);
        JsonNode assetsNode = latestReleaseNode.get("assets");

        String tagName = latestReleaseNode.get("tag_name").asText();

        for (Iterator<JsonNode> it = assetsNode.elements(); it.hasNext(); ) {
            JsonNode asset = it.next();
            if (asset.get("name").asText().contains("-jar-with-dependencies.jar")) {
                String downloadUrl = asset.get("browser_download_url").asText();

                try {
                    IOUtils.copy(new URL(downloadUrl), new File("Ree6.jar"));
                    print("We downloaded the newest Ree6 version! (" + tagName + ")");
                } catch (Exception exception) {
                    print("We could not download the newest Ree6 version!\nManually download it over " + downloadUrl);
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
        config.getConfiguration().set("hikari.misc.storage", "sqllite");

        print("You selected SQLLite!\nYou dont need to set up anything for this one!");

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