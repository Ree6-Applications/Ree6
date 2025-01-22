package de.presti.ree6.addons;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.pf4j.*;
import org.pf4j.util.FileUtils;
import org.pf4j.util.StringUtils;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class YamlPluginDescriptorFinder implements PluginDescriptorFinder {

    public static final String DEFAULT_YAML_FILE_NAME = "plugin.yml";

    public static final String PLUGIN_ID = "id";
    public static final String PLUGIN_DESCRIPTION = "description";
    public static final String PLUGIN_CLASS = "class";
    public static final String PLUGIN_VERSION = "version";
    public static final String PLUGIN_PROVIDER = "provider";
    public static final String PLUGIN_DEPENDENCIES = "dependencies";
    public static final String PLUGIN_REQUIRES = "requires";
    public static final String PLUGIN_LICENSE = "license";

    protected String yamlFileName;

    public YamlPluginDescriptorFinder() {
        this(DEFAULT_YAML_FILE_NAME);
    }

    public YamlPluginDescriptorFinder(String yamlFileName) {
        this.yamlFileName = yamlFileName;
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
        YamlConfiguration yamlConfiguration;
        try (ZipFile zip = new ZipFile(pluginPath.toFile())) {
            log.debug("Lookup plugin descriptor in '{}'", pluginPath);
            ZipEntry pluginEntry = zip.getEntry(yamlFileName);

            if (pluginEntry == null) {
                throw new PluginRuntimeException("Cannot find 'plugin.yml' in {}", pluginPath);
            }

            try (InputStream input = zip.getInputStream(pluginEntry)) {
                // Don't use the input stream directly YAML will cry.
                yamlConfiguration = YamlConfiguration.loadConfigurationFromString(String.join("\n", IOUtils.readLines(input, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new PluginRuntimeException(e);
            }
        } catch (IOException exception) {
            throw new PluginRuntimeException(exception);
        } finally {
            FileUtils.closePath(pluginPath);
        }

        return yamlConfiguration;
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
