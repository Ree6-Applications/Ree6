package de.presti.ree6.sql.entities;

import com.google.gson.JsonArray;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Tickets")
public class Tickets {

    /**
     * The ID of the Guild.
     */
    @Id
    @Getter
    @Setter
    @Column(name = "guildId")
    private Long guildId;

    /**
     * The ID of the Channel.
     */
    @Getter
    @Setter
    @Column(name = "channelId")
    long channelId;

    /**
     * The Category ID of the Tickets.
     */
    @Getter
    @Setter
    @Column(name = "ticketCategory")
    long ticketCategory;

    /**
     * The Category ID of the Archive category.
     */
    @Getter
    @Setter
    @Column(name = "archiveCategory")
    long archiveCategory;

    /**
     * The Ticket counter.
     */
    @Getter
    @Setter
    @Column(name = "ticketCount")
    long ticketCount;

    /**
     * A {@link JsonArray} list with all Ticket IDs and data related to it.
     */
    @Convert
    @Getter
    @Setter
    @Column(name = "tickets")
    JsonArray ticketData;
}
