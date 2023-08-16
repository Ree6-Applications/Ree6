package de.presti.ree6.utils.data;

/**
 * Utility class meant to store all the RegExes.
 */
public class RegExUtil {

    /**
     * The Regex for URLs.
     */
    public static final String URL_REGEX = "^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)$";

    /**
     * The Regex for TikTok Links.
     */
    public static final String TIKTOK_REGEX = "^https?:\\/\\/(?:www\\.)?tiktok\\.com\\/(?:(?:share\\/(?:video|user)\\/\\d+)|(?:@[\\w.]+(?:\\/video\\/\\d+)?))$";

    /**
     * The Regex for Spotify Links.
     */
    public static final String SPOTIFY_REGEX = "^(https?://(?:open\\.)?spotify\\.com/(?:[\\w-]+/)*?(embed|track|album|playlist|artist)/(\\w+)).*";

    /**
     * RegEx to detect a Blerp link in the redemption itself.
     */
    public static final String BLERP_REGEX = "https:\\/\\/blerp\\.com\\/soundbites\\/[a-zA-Z0-9]+";

    /**
     * RegEx to take the CDN Url of that sound out of the HTML content.
     */
    public static final String BLERP_PAGE_REGEX = "https:\\/\\/cdn\\.blerp\\.com\\/normalized\\/[a-zA-Z0-9]+";

    /**
     * The Regex for YouTube Links.
     */
    public static final String YOUTUBE_REGEX = "^(?:(?:https?:\\/\\/)?(?:www\\.)?youtu\\.be\\/|^(?:(?:(?:https?:\\/\\/)?(?:www\\.)?youtube\\.com\\/watch\\?.*v=)|(?:https?:\\/\\/)?(?:www\\.)?youtube\\.com\\/v\\/))([a-zA-Z0-9_-]{11})";

}
