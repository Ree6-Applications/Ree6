package de.presti.ree6.actions.streamtools.container;

import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.action.*;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.StreamAction;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Utility class used to create StreamActionContainers.
 */
public class StreamActionContainerCreator {

    /**
     * A Cache for all StreamActions.
     */
    private static final HashMap<String, Class<? extends IStreamAction>> cachedActions = new HashMap<>();

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     */
    public StreamActionContainerCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Load all actions.
     */
    public static void loadAll() {
        cachedActions.put("voice-join", VoiceJoinStreamAction.class);
        cachedActions.put("voice-leave", VoiceLeaveStreamAction.class);
        cachedActions.put("play-url", PlayUrlStreamAction.class);
        cachedActions.put("play-tts", PlayTTSStreamAction.class);
        cachedActions.put("say", SayStreamAction.class);
    }

    /**
     * Get the Class of a StreamAction.
     * @param action The Name of the Action.
     * @return The Class of the Action.
     */
    public static Class<? extends IStreamAction> getAction(String action) {
        action = action.trim().toLowerCase();

        if (cachedActions.containsKey(action))
            return cachedActions.get(action);

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.addClassLoaders(ClasspathHelper.staticClassLoader());
        Reflections reflections = new Reflections(configurationBuilder);
        Set<Class<? extends IStreamAction>> classes = reflections.getSubTypesOf(IStreamAction.class);

        for (Class<? extends IStreamAction> aClass : classes) {
            if (aClass.isAnnotationPresent(ActionInfo.class) && aClass.getAnnotation(ActionInfo.class).name().trim().equalsIgnoreCase(action)) {
                if (!cachedActions.containsKey(action)) {
                    cachedActions.put(action, aClass);
                }

                return aClass;
            }
        }

        return null;
    }

    /**
     * Get all StreamActionContainers.
     *
     * @param listenerId The Listener to get the Containers for.
     * @return A List of StreamActionContainers.
     */
    public static List<StreamActionContainer> getContainers(int listenerId) {
        return SQLSession.getSqlConnector().getSqlWorker()
                .getEntityList(new StreamAction(), "FROM StreamAction WHERE listener = :listener", Map.of("listener", listenerId))
                .stream().map(StreamActionContainer::new).toList();
    }

}
