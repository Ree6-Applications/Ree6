package de.presti.ree6.game.impl.musicquiz.entities;

import lombok.Getter;

/**
 * Classed used to store information about a song for the music quiz game.
 */
@Getter
public class MusicQuizEntry {

    /**
     * The Name of the Artist.
     */
    String artist;

    /**
     * The Name of the Song.
     */
    String title;

    /**
     * An array of all features.
     */
    String[] features;

    /**
     * The URL of the Song.
     */
    String audioUrl;

    /**
     * Value to know if the artists has already been guessed.
     */
    boolean artistGuessed;

    /**
     * Value to know if the title has already been guessed.
     */
    boolean titleGuessed;

    /**
     * Value to know if the features have already been guessed.
     */
    boolean featuresGuessed;

    /**
     * Constructor for the MusicQuizEntry. <br>
     * This constructor is used to create a new MusicQuizEntry without guessed values!
     *
     * @param musicQuizEntry The MusicQuizEntry that should be copied.
     */
    public MusicQuizEntry(MusicQuizEntry musicQuizEntry) {
        this.artist = musicQuizEntry.getArtist();
        this.title = musicQuizEntry.getTitle();
        this.features = musicQuizEntry.getFeatures();
        this.audioUrl = musicQuizEntry.getAudioUrl();
    }

    /**
     * Constructor for the MusicQuizEntry.
     *
     * @param artist   The Name of the Artist.
     * @param title    The Name of the Song.
     * @param features An array of all features.
     * @param audioUrl The URL of the Song.
     */
    public MusicQuizEntry(String artist, String title, String[] features, String audioUrl) {
        this.artist = artist;
        this.title = title;
        this.features = features;
        this.audioUrl = audioUrl;
    }

    /**
     * Check if a string contains the name of the artists.
     *
     * @param text The text that should be checked.
     * @return True if the text contains the name of the artist.
     */
    public boolean checkArtist(String text) {
        if (artistGuessed) return false;

        if (text.contains(this.artist)) {
            artistGuessed = true;
            return true;
        }
        return false;
    }

    /**
     * Check if a string contains the name of the title.
     *
     * @param text The text that should be checked.
     * @return True if the text contains the name of the title.
     */
    public boolean checkTitle(String text) {
        if (titleGuessed) return false;

        if (text.contains(this.title)) {
            titleGuessed = true;
            return true;
        }
        return false;
    }

    /**
     * Check if a string contains the name of the features.
     *
     * @param text The text that should be checked.
     * @return True if the text contains the name of the features.
     */
    public boolean checkFeatures(String text) {
        if (featuresGuessed) return false;

        for (String s : this.features) {
            if (text.contains(s)) {
                featuresGuessed = true;
                return true;
            }
        }
        return false;
    }
}
