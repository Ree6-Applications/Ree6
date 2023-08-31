package de.presti.ree6.module;

import java.util.List;

/**
 * Interface for a simple Manager for a specific Entity.
 * @param <R> the Entity typ.
 */
public interface IManager<R> {

    /**
     * Load the needed data.
     */
    void load();

    /**
     * Add an object to the List.
     * @param object the object to add.
     */
    default void add(R object) {
        if (!getList().contains(object)) {
            getList().add(object);
        }
    }

    /**
     * Remove an object from the List.
     * @param object the object to remove.
     */
    default void remove(R object) {
        getList().remove(object);
    }

    /**
     * Clear the List.
     */
    default void clear() {
        getList().clear();
    }

    /**
     * Replace the List with a new one.
     * @param newList the new List.
     */
    default void replace(List<R> newList) {
        clear();
        getList().addAll(newList);
    }

    /**
     * Get an object from the List.
     * @param value the value to search for.
     * @return the object.
     * @throws UnsupportedOperationException if the method is not implemented.
     */
    default R get(String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get an object from the List.
     * @param value the value to search for.
     * @return the object.
     * @throws UnsupportedOperationException if the method is not implemented.
     */
    default R get(long value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the List.
     * @return the List.
     */
    List<R> getList();
}
