package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.entitis.SQLEntity;

/**
 * SQL Entity for the Blacklist.
 */
@Table(name = "ChatProtector")
public class Blacklist extends SQLEntity {

    /**
     * The ID of the Guild.
     */
    @Property(name = "gid")
    private String guildId;

    /**
     * The blacklisted word.
     */
    @Property(name = "word")
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
}
