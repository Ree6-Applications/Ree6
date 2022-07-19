package de.presti.ree6.sql.base.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as a property of a SQL table.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {

    /**
     * The Name of SQL Column.
     *
     * @return column name.
     */
    String name();

    /**
     * Check if the property is the primary of the Table or not.
     *
     * @return if it is or not.
     */
    boolean primary() default false;

    /**
     * Check if the property should be used in the update SQL query.
     * @return if it should be used or not.
     */
    boolean updateQuery() default false;
}
