package de.presti.ree6.sql.base.data;

import java.util.ArrayList;

public class SQLResponse {

    ArrayList<Class<?>> entities = new ArrayList<>();

    Class<?> entity = null;

    public SQLResponse(Class<?> entity) {
        this.entity = entity;
    }

    public SQLResponse(ArrayList<Class<?>> entities) {
        this.entities = entities;
    }

    public Class<?> getEntity() {
        return entity;
    }

    public ArrayList<Class<?>> getEntities() {
        return entities;
    }

}
