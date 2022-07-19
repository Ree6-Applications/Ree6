package de.presti.ree6.sql.seed;

import de.presti.ree6.main.Main;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Class used to manage Seeds.
 */
public class SeedManager {

    /**
     * Method called to run all Seeds.
     */
    public void runAllSeeds() {
        Reflections reflections = new Reflections("de.presti.ree6");
        Set<Class<? extends Seed>> classes = reflections.getSubTypesOf(Seed.class);
        for (Class<? extends Seed> aClass : classes) {
            try {
                Seed seed = aClass.getDeclaredConstructor().newInstance();
                Main.getInstance().getLogger().info("Trying to run Seed " + seed.getClass().getName());
                seed.run();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                e.printStackTrace();
                Main.getInstance().getLogger().error("Could not run Seed!");
            }
        }
    }

}
