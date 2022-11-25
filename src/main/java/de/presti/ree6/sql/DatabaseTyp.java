package de.presti.ree6.sql;

public enum DatabaseTyp {

    MariaDB("jdbc:mariadb://%s:%s/%s", "org.hibernate.dialect.MariaDBDialect"),

    SQLite("jdbc:sqlite:%s", "org.hibernate.community.dialect.SQLiteDialect");

    private final String jdbcURL;

    private final String hibernateDialect;

    DatabaseTyp(String jdbcURL, String hibernateDialect) {
        this.jdbcURL = jdbcURL;
        this.hibernateDialect = hibernateDialect;
    }

    public String getJdbcURL() {
        return jdbcURL;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }
}
