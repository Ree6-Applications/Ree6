package de.presti.ree6.utils.apis;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.data.RegExUtil;
import de.presti.wrapper.YouTubeWrapper;
import de.presti.wrapper.entities.VideoResult;
import de.presti.wrapper.entities.channel.ChannelResult;
import de.presti.wrapper.entities.channel.ChannelShortResult;
import de.presti.wrapper.entities.channel.ChannelVideoResult;
import de.presti.wrapper.entities.search.ChannelSearchResult;
import de.presti.wrapper.entities.search.SearchResult;
import io.sentry.Sentry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// TODO:: check if there is a way to make this more efficient, maybe use a cache system or merge multiple requests into one and split the result for further use again?

/**
 * YouTubeAPIHandler.
 */
@Slf4j
public class YouTubeAPIHandler {

    /**
     * The YouTube API-Handler.
     */
    private static YouTubeAPIHandler instance;

    /**
     * The Pattern for YouTube Links.
     */
    @Getter
    private static final Pattern pattern = Pattern.compile(RegExUtil.YOUTUBE_REGEX);

    /**
     * Constructor.
     */
    public YouTubeAPIHandler() {
        instance = this;
    }

    /**
     * Search on YouTube a specific query.
     *
     * @param search The query.
     * @return A link to the first Video result.
     * @throws Exception if there was a search problem.
     */
    public String searchYoutube(String search) throws Exception {
        List<SearchResult> results = YouTubeWrapper.search(search, SearchResult.FILTER.VIDEO);

        if (!results.isEmpty()) {
            String videoId = results.get(0).getId();

            return "https://www.youtube.com/watch?v=" + videoId;
        }

        return null;
    }

    /**
     * Get the YouTube uploads of a specific user.
     *
     * @param channelId The channel id.
     * @return A list of all Video ids.
     */
    public List<VideoResult> getYouTubeUploads(String channelId) throws IOException, InterruptedException, IllegalAccessException {
        List<VideoResult> playlistItemList = new ArrayList<>();

        if (isValidChannelId(channelId)) {
            if (BotConfig.isDebug())
                log.info("Getting videos for channel: " + channelId);
            ChannelVideoResult channelVideo = YouTubeWrapper.getChannelVideo(channelId);

            // Convert it to an actual Video instead of a stripped down version.
            for (VideoResult video : channelVideo.getVideos()) {
                try {
                    // We are doing this to get the full video object,
                    // because the channel video result only contains a stripped down version of the video.
                    // Mainly because of upload information.
                    playlistItemList.add(YouTubeWrapper.getVideo(video.getId(), false));
                } catch (Exception exception) {
                    Sentry.captureException(exception);
                }
            }

            ChannelShortResult channelShorts = YouTubeWrapper.getChannelShort(channelId);

            for (VideoResult shorts : channelShorts.getShorts()) {
                try {
                    // Same as above, but for shorts.
                    playlistItemList.add(YouTubeWrapper.getVideo(shorts.getId(), true));
                } catch (Exception exception) {
                    Sentry.captureException(exception);
                }
            }
        }

        return playlistItemList;
    }

    /**
     * Get an YouTube channel by id.
     *
     * @param channelName The channel name.
     * @return The channel.
     * @throws Exception if something went wrong.
     */
    public ChannelResult getYouTubeChannelBySearch(String channelName) throws Exception {
        List<SearchResult> searchResults = YouTubeWrapper.search(channelName, SearchResult.FILTER.CHANNEL);

        if (searchResults.isEmpty()) {
            return null;
        }

        ChannelSearchResult channelSearchResult = (ChannelSearchResult)searchResults.get(0);

        if (channelSearchResult == null) {
            return null;
        }

        return YouTubeWrapper.getChannel(channelSearchResult.getId());
    }

    /**
     * Get an YouTube channel by id.
     *
     * @param channelId The channel id.
     * @return The channel.
     * @throws Exception if something went wrong.
     */
    public ChannelResult getYouTubeChannelById(String channelId) throws Exception {
        return YouTubeWrapper.getChannel(channelId);
    }

    /**
     * Check if a given channel ID matches the pattern of a YouTube channel ID.
     *
     * @param channelId The channel ID.
     * @return True if it matches, false if not.
     */
    public boolean isValidChannelId(String channelId) {
        return channelId.matches("^UC[\\w-]{21}[AQgw]$");
    }


    /**
     * Method used to return an instance of the handler.
     *
     * @return instance of the handler.
     */
    public static YouTubeAPIHandler getInstance() {
        if (instance == null) {
            return instance = new YouTubeAPIHandler();
        }
        return instance;
    }
}
