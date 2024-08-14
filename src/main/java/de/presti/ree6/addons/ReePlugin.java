package de.presti.ree6.addons;

import org.apache.commons.digester.plugins.PluginContext;
import org.pf4j.Plugin;

/**
 * The Interface used for the Addons.
 */
public abstract class ReePlugin extends Plugin {

    protected final PluginContext context;

    protected ReePlugin(PluginContext context) {
        super();
        this.context = context;
    }
}
