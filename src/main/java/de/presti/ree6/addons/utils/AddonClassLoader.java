package de.presti.ree6.addons.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class AddonClassLoader extends URLClassLoader {

    public static final List<String> SHARED_PACKAGES = Arrays.asList(
            "de.ree6.sql",
            "de.presti.ree6.commands",
            "de.presti.ree6.bot.util",
            "de.presti.ree6.utils",
            "de.presti.ree6.module",
            "de.presti.ree6.main",
            "de.presti.ree6.language",
            "de.presti.ree6.game",
            "de.presti.ree6.audio"
    );

    public AddonClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // has the class loaded already?
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            final boolean isSharedClass = SHARED_PACKAGES.stream().anyMatch(name::startsWith);
            if (isSharedClass) {
                loadedClass = getParent().loadClass(name);
            } else {
                loadedClass = super.loadClass(name, resolve);
            }
        }

        if (resolve) {      // marked to resolve
            resolveClass(loadedClass);
        }
        return loadedClass;
    }
}
