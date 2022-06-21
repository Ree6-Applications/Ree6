package de.presti.ree6.sql.base.data;

import java.util.ArrayList;

/**
 * This class is used to represent a SQL Response.
 */
public class SQLResponse {

    /**
     * The data of the response.
     */
    ArrayList<Object> entities = new ArrayList<>();

    /**
     * The data of the response.
     */
    Object entity = null;

    /**
     * Constructor.
     *
     * @param entity The data of the response.
     */
    public SQLResponse(Object entity) {
        this.entity = entity;
    }

    /**
     * Constructor.
     *
     * @param entities The data of the response.
     */
    public SQLResponse(ArrayList<Object> entities) {
        this.entities = entities;
    }

    /**
     * Get the data of the response.
     *
     * @return The data of the response.
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * Get the data of the response.
     *
     * @return The data of the response.
     */
    public ArrayList<Object> getEntities() {
        return entities;
    }

}
