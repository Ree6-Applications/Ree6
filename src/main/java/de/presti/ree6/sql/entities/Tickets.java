package de.presti.ree6.sql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * SQL Entity for the Tickets.
 */
@Getter
@Setter
@Entity
@Table(name = "Tickets")
public class Tickets {

    /**
     * The ID of the Guild.
     */
    @Id
    @Column(name = "guildId")
    private Long guildId;

    /**
     * The ID of the Channel.
     */
    @Column(name = "channelId")
    long channelId;

    /**
     * The Category ID of the Tickets.
     */
    @Column(name = "ticketCategory")
    long ticketCategory;

    /**
     * The Category ID of the Archive category.
     */
    @Column(name = "archiveCategory")
    long archiveCategory;

    /**
     * The Ticket counter.
     *
     */
    @Column(name = "ticketCount")
    @ColumnDefault("0")
    long ticketCount;
}
