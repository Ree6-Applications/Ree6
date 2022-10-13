package de.presti.ree6.sql.entities;

import com.google.gson.JsonArray;
import jakarta.persistence.*;

@Entity
@Table(name = "tickets")
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
     */
    @Column(name = "ticketCount")
    long ticketCount;

    /**
     * A {@link JsonArray} list with all Ticket IDs and data related to it.
     */
    @Convert
    @Column(name = "tickets")
    JsonArray ticketData;

    public void setGuildId(Long id) {
        this.guildId = id;
    }

    public Long getGuildId() {
        return guildId;
    }
}
