package de.presti.ree6.sql.entities;

import jakarta.persistence.*;

/**
 * SQL Entity for the Blacklist.
 */
@Entity
@Table(name = "ChatProtector")
public class Blacklist {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * The ID of the Guild.
     */
    @Column(name = "gid")
    private String guildId;

    /**
     * The blacklisted word.
     */
    @Column(name = "word")
    private String word;

    /**
     * Constructor.
     */
    public Blacklist() {
    }

    /**
     * Constructor.
     *
     * @param guildId the GuildID of the Blacklist.
     * @param word    the blacklisted word.
     */
    public Blacklist(String guildId, String word) {
        this.guildId = guildId;
        this.word = word;
    }

    /**
     * Get the GuildID of the Blacklist.
     *
     * @return {@link String} as GuildID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the blacklisted word.
     *
     * @return {@link String} as blacklisted word.
     */
    public String getWord() {
        return word;
    }

    /**
     * Override to just return the Word.
     * @return {@link String} as Word.
     */
    @Override
    public String toString() {
        return getWord();
    }
}
