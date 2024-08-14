package de.presti.ree6.addons;

import lombok.Getter;
import org.pf4j.RuntimeMode;

@Getter
public class ReePluginContext {

    private final RuntimeMode runtimeMode;

    public ReePluginContext(RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
    }

}
