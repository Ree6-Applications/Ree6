package de.presti.ree6.sql.base.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

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
    Object entity = new SQLEntity();

    /**
     * boolean to inform if the request was successful.
     */
    boolean isSuccess = false;

    /**
     * Constructor.
     *
     * @param entity The data of the response.
     */
    public SQLResponse(Object entity) {
        if (entity != null)
            isSuccess = true;

        this.entity = Objects.requireNonNullElseGet(entity, SQLEntity::new);
    }

    /**
     * Constructor.
     *
     * @param entities The data of the response.
     */
    public SQLResponse(ArrayList<Object> entities) {
        if (entities != null)
            isSuccess = true;

        this.entities = Objects.requireNonNullElseGet(entities, () -> (ArrayList<Object>) Collections.emptyList());

        if (entities != null && !entities.isEmpty()) {
            entity = entities.get(0);
        }
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

    /**
     * Gives you the information if not result was found.
     * @return True if no result was found.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

}
