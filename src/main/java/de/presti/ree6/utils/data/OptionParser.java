package de.presti.ree6.utils.data;

import java.util.HashMap;

/**
 * Class used to parse options.
 * @author CodeManDev
 * <a href="https://github.com/DuzeyYT/RiseFreeLauncher/blob/master/src/main/java/cc/fish/rfl/api/utils/OptionParser.java">Source</a>
 */
public class OptionParser {
    private final HashMap<String, String> options;

    /**
     * Constructor to parse the options.
     * @param args the CLI Arguments.
     * @param addEnv if environment values should be added to the list.
     */
    public OptionParser(String[] args, boolean addEnv) {
        this.options = new HashMap<>();

        for (String arg : args) {
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String value = "";
                if (key.contains("=")) {
                    value = key.substring(key.indexOf("=") + 1);
                    key = key.split("=")[0];
                }
                this.options.put(key, value);
            }
        }

        if (addEnv) {
            options.putAll(System.getenv());
        }
    }

    /**
     * Check if an option is present.
     * @param name the name of the option.
     * @return true, if present.
     */
    public boolean isEnabled(String name) {
        return this.options.containsKey(name);
    }

    /**
     * Get the value of an option.
     * @param name the name of the option.
     * @return value.
     */
    public String getValue(String name) {
        return this.options.get(name);
    }

    /**
     * Get the value of an option or a default.
     * @param name the name of the option.
     * @param defaultValue default value if the option isn't set.
     * @return value.
     */
    public String getValueOrDefault(String name, String defaultValue) {
        return this.options.getOrDefault(name, defaultValue);
    }
}
