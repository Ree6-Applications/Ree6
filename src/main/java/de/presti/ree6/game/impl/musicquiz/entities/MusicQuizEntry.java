package de.presti.ree6.game.impl.musicquiz.entities;

public class MusicQuizEntry {

    String artist;
    String title;
    String[] features;

    String audioUrl;

    boolean artistGuessed;
    boolean titleGuessed;
    boolean featuresGuessed;

    public MusicQuizEntry(MusicQuizEntry musicQuizEntry) {
        this.artist = musicQuizEntry.getArtist();
        this.title = musicQuizEntry.getTitle();
        this.features = musicQuizEntry.getFeatures();
        this.audioUrl = musicQuizEntry.getAudioUrl();
    }

    public MusicQuizEntry(String artist, String title, String[] features, String audioUrl) {
        this.artist = artist;
        this.title = title;
        this.features = features;
        this.audioUrl = audioUrl;
    }

    public boolean checkArtist(String artist) {
        if (artistGuessed) return false;

        if (artist.contains(this.artist)) {
            artistGuessed = true;
            return true;
        }
        return false;
    }

    public boolean checkTitle(String title) {
        if (titleGuessed) return false;

        if (title.contains(this.title)) {
            titleGuessed = true;
            return true;
        }
        return false;
    }

    public boolean checkFeatures(String features) {
        if (featuresGuessed) return false;

        for (String s : this.features) {
            if (features.contains(s)) {
                featuresGuessed = true;
                return true;
            }
        }
        return false;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String[] getFeatures() {
        return features;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public boolean isArtistGuessed() {
        return artistGuessed;
    }

    public boolean isTitleGuessed() {
        return titleGuessed;
    }

    public boolean isFeaturesGuessed() {
        return featuresGuessed;
    }
}
