package de.presti.ree6.module.notifications;

import de.presti.ree6.module.IManager;
import de.presti.ree6.sql.entities.stats.ChannelStats;

import java.util.List;

/**
 * Interface for Secure Online Notification Interoperable Class Modules.
 */
public interface ISonic extends IManager<SonicIdentifier> {

    default void load(List<ChannelStats> channelStats) {
        load();
    }
    void load();
    void run();
    default void unload() {
        clear();
    }

    default void add(long identifier) {
        add(new SonicIdentifier(String.valueOf(identifier)));
    }

    default void add(String identifier) {
        add(new SonicIdentifier(identifier));
    }

    default boolean contains(long identifier) {
        return getList().stream().anyMatch(x -> x.getIdentifierAsLong() == identifier);
    }

    default boolean contains(String identifier) {
        return getList().stream().anyMatch(x -> x.getIdentifier().equals(identifier));
    }

    default boolean contains(SonicIdentifier identifier) {
        return getList().stream().anyMatch(x -> x.getIdentifier().equals(identifier.getIdentifier()));
    }

    default void remove(long identifier) {
        remove(get(identifier));
    }

    default void remove(String identifier) {
        remove(get(identifier));
    }
}
