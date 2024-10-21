package de.presti.ree6.addons;

import org.pf4j.Plugin;

/**
 * The Interface used for the Addons.
 */
public abstract class ReePlugin extends Plugin {

    protected final ReePluginContext context;

    protected ReePlugin(ReePluginContext context) {
        super();
        this.context = context;
    }
}
