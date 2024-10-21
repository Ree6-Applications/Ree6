package de.presti.ree6.addons;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import java.lang.reflect.Constructor;

@Slf4j
public class ReePluginFactory extends DefaultPluginFactory {

    @Override
    protected Plugin createInstance(Class<?> pluginClass, PluginWrapper pluginWrapper) {
        ReePluginContext context = new ReePluginContext(pluginWrapper.getRuntimeMode());
        try {
            Constructor<?> constructor = pluginClass.getConstructor(ReePluginContext.class);
            return (Plugin) constructor.newInstance(context);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }
}
