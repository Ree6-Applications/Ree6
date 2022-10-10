package de.presti.ree6.sql.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Opt_out")
public class OptOut {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "gid")
    String guildId;

    @Column(name = "uid")
    String userId;

    public OptOut() {
    }

    public OptOut(String guildId, String userId) {
        this.guildId = guildId;
        this.userId = userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
