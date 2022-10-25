package de.presti.ree6.sql.entities.stats;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SQL Entity for the Guild-Stats.
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "GuildStats")
public class GuildCommandStats {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * The Guild ID.
     */
    @Getter
    @Column(name = "gid")
    private String guildId;

    /**
     * Name of the Command.
     */
    @Getter
    @Column(name = "command")
    private String command;

    /**
     * Number of times the Command was used.
     */
    @Getter
    @Setter
    @Column(name = "uses")
    private int uses;
}
