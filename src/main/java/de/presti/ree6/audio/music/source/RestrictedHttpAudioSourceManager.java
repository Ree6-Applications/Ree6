package de.presti.ree6.audio.music.source;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

import java.util.Arrays;

/**
 * A restricted version of {@link HttpAudioSourceManager}.
 */
public class RestrictedHttpAudioSourceManager extends HttpAudioSourceManager {

    /**
     * A whitelist for every URL that is allowed.
     */
    static final String[] WHITELIST = {
            "https://cdn.blurp.com",
            "https://api.streamelements.com/kappa/v2/speech",
    };

    /**
     * A {@link AudioReference} for later use as return value if the URL is not whitelisted.
     */
    static final AudioReference invalidReference = new AudioReference("INVALID", "Blocked URL!");

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        reference = checkWhitelisting(reference);
        return super.loadItem(manager, reference);
    }

    /**
     * A method used to check everything
     * @param reference the {@link AudioReference} parsed from above.
     * @return either the invalid Reference or the input back.
     */
    public AudioReference checkWhitelisting(AudioReference reference) {
        String url = reference.identifier;

        if (Arrays.stream(WHITELIST).anyMatch(url::startsWith)) {
            return reference;
        }

        return invalidReference;
    }
}
