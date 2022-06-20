package de.presti.ree6.sql.base.data;

import java.util.ArrayList;

public class SQLResponse {

    ArrayList<Object> entities = new ArrayList<>();

    Object entity = null;

    public SQLResponse(Object entity) {
        this.entity = entity;
    }

    public SQLResponse(ArrayList<Object> entities) {
        this.entities = entities;
    }

    public Object getEntity() {
        return entity;
    }

    public ArrayList<Object> getEntities() {
        return entities;
    }

}
