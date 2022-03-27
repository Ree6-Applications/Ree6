package de.presti.ree6.utils.apis;

import pw.aru.api.nekos4j.Nekos4J;

public class Neko4JsAPI {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private Neko4JsAPI() {
        throw new IllegalStateException("Utility class");
    }

    public static final Nekos4J imageAPI = new Nekos4J.Builder().build();
}
