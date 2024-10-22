package de.presti.ree6.addons;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginFactory;

@Slf4j
public class ReePluginManager extends DefaultPluginManager {

    public ReePluginManager() {
        super();
        addPluginStateListener(x -> {
            switch (x.getPluginState()) {
                case STARTED -> log.info("Plugin '{}' started", x.getPlugin().getPluginId());
                case FAILED -> log.error("Plugin '{}' failed", x.getPlugin().getPluginId());
                case STOPPED -> log.info("Plugin '{}' stopped", x.getPlugin().getPluginId());
                case DISABLED -> log.info("Plugin '{}' disabled", x.getPlugin().getPluginId());
                case UNLOADED -> log.info("Plugin '{}' unloading", x.getPlugin().getPluginId());
                case CREATED -> log.info("Plugin '{}' created", x.getPlugin().getPluginId());
                case RESOLVED -> log.info("Plugin '{}' resolved", x.getPlugin().getPluginId());
            }
        });
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new YamlPluginDescriptorFinder();
    }

    @Override
    protected PluginFactory createPluginFactory() {
        return new ReePluginFactory();
    }
}
