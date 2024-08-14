package de.presti.ree6.addons;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.*;
import org.pf4j.util.FileUtils;
import org.pf4j.util.StringUtils;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class YamlPluginDescriptorFinder implements PluginDescriptorFinder {

    public static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.yml";

    public static final String PLUGIN_ID = "id";
    public static final String PLUGIN_DESCRIPTION = "description";
    public static final String PLUGIN_CLASS = "class";
    public static final String PLUGIN_VERSION = "version";
    public static final String PLUGIN_PROVIDER = "provider";
    public static final String PLUGIN_DEPENDENCIES = "dependencies";
    public static final String PLUGIN_REQUIRES = "requires";
    public static final String PLUGIN_LICENSE = "license";

    protected String propertiesFileName;

    public YamlPluginDescriptorFinder() {
        this(DEFAULT_PROPERTIES_FILE_NAME);
    }

    public YamlPluginDescriptorFinder(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && (Files.isDirectory(pluginPath) || FileUtils.isZipOrJarFile(pluginPath));
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        YamlConfiguration properties = readYaml(pluginPath);

        return createPluginDescriptor(properties);
    }

    protected YamlConfiguration readYaml(Path pluginPath) {
        Path yamlPath = getYamlPath(pluginPath, propertiesFileName);
        if (yamlPath == null) {
            throw new PluginRuntimeException("Cannot find the properties path");
        }

        YamlConfiguration yamlConfiguration = null;
        try {
            log.debug("Lookup plugin descriptor in '{}'", yamlPath);
            if (Files.notExists(yamlPath)) {
                throw new PluginRuntimeException("Cannot find '{}' path", yamlPath);
            }

            try (InputStream input = Files.newInputStream(yamlPath)) {
                yamlConfiguration = YamlConfiguration.loadConfiguration(input);
            } catch (IOException e) {
                throw new PluginRuntimeException(e);
            }
        } finally {
            FileUtils.closePath(yamlPath);
        }

        return yamlConfiguration;
    }

    protected Path getYamlPath(Path pluginPath, String propertiesFileName) {
        if (Files.isDirectory(pluginPath)) {
            return pluginPath.resolve(Paths.get(propertiesFileName));
        }

        // it's a zip or jar file
        try {
            return FileUtils.getPath(pluginPath, propertiesFileName);
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        }
    }

    protected PluginDescriptor createPluginDescriptor(YamlConfiguration yamlConfiguration) {
        String id = yamlConfiguration.getString(PLUGIN_ID);

        String description = yamlConfiguration.getString(PLUGIN_DESCRIPTION);
        if (StringUtils.isNullOrEmpty(description)) {
            description = "";
        }

        String clazz = yamlConfiguration.getString(PLUGIN_CLASS);
        String version = yamlConfiguration.getString(PLUGIN_VERSION);
        String provider = yamlConfiguration.getString(PLUGIN_PROVIDER);
        List<String> dependencies = yamlConfiguration.getStringList(PLUGIN_DEPENDENCIES);
        String requires = yamlConfiguration.getString(PLUGIN_REQUIRES);
        String license = yamlConfiguration.getString(PLUGIN_LICENSE);

        DefaultPluginDescriptor pluginDescriptor = createPluginDescriptorInstance(id, description, clazz, version, requires, provider, license);

        for (String dependency : dependencies) {
            pluginDescriptor.addDependency(new PluginDependency(dependency));
        }

        return pluginDescriptor;
    }

    protected DefaultPluginDescriptor createPluginDescriptorInstance(String id, String description, String clazz, String version, String requires, String provider, String license) {
        return new DefaultPluginDescriptor(id, description, clazz, version, requires, provider, license);
    }
}
