package de.presti.ree6.sql.base.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to represent a SQL Response.
 */
public class SQLResponse {

    /**
     * The data of the response.
     */
    List<Object> entities = new ArrayList<>();

    /**
     * The data of the response.
     */
    Object entity = new SQLEntity();

    /**
     * Used for a successful check.
     */
    private final SQLEntity emptyEntity = new SQLEntity();

    /**
     * Constructor.
     *
     * @param entity The data of the response.
     */
    public SQLResponse(Object entity) {
        this.entity = Objects.requireNonNullElseGet(entity, SQLEntity::new);
    }

    /**
     * Constructor.
     *
     * @param entities The data of the response.
     */
    public SQLResponse(List<Object> entities) {
        this.entities = Objects.requireNonNullElseGet(entities, ArrayList::new);

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
    public List<Object> getEntities() {
        return entities;
    }

    /**
     * Gives you the information if not result was found.
     * @return True if no result was found.
     */
    public boolean isSuccess() {
        return entity != null && !entity.getClass().isInstance(emptyEntity);
    }
}
