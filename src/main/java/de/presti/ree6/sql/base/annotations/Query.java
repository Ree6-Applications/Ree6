package de.presti.ree6.sql.base.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * The wanted custom Query.
     *
     * @return custom Query.
     */
    String query();

}
