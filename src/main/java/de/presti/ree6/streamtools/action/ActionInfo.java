package de.presti.ree6.streamtools.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ActionInfo {
    String name();
    String command();
    String description();
    String introduced();
}
