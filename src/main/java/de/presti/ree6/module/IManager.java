package de.presti.ree6.module;

import java.util.List;

public interface IManager<R> {

    void load();

    default void add(R object) {
        if (!getList().contains(object)) {
            getList().add(object);
        }
    }

    default void remove(R object) {
        getList().remove(object);
    }

    default void clear() {
        getList().clear();
    }

    default void replace(List<R> newList) {
        clear();
        getList().addAll(newList);
    }

    default R get(String value) {
        throw new UnsupportedOperationException();
    }

    default R get(long value) {
        throw new UnsupportedOperationException();
    }
    List<R> getList();
}
