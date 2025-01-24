package de.presti.ree6.addons;

import lombok.Getter;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;

@Getter
public class ReePluginContext {

    private final RuntimeMode runtimeMode;
    private final Logger logger;

    public ReePluginContext(RuntimeMode runtimeMode, Logger logger) {
        this.runtimeMode = runtimeMode;
        this.logger = logger;
    }

}
