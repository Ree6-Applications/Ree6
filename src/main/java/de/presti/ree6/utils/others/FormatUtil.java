/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.presti.ree6.utils.others;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * From <a href="https://github.com/jagrosh/MusicBot">J MusicBot</a>
 * @author <a href="mailto:john.a.grosh@gmail.com">John Grosh</a>
 */
public class FormatUtil {

    /**
     * A String containing the UTF-8 character for a play button.
     */
    public static final String PLAY_EMOJI  = "\u25B6";

    /**
     * A String containing the UTF-8 character for a pause button.
     */
    public static final String PAUSE_EMOJI = "\u23F8";

    /**
     * A String containing the UTF-8 character for a stop button.
     */
    public static final String STOP_EMOJI  = "\u23F9";

    /**
     * A String containing the UTF-8 character for a repeat button.
     */
    public static final String LOOP_EMOJI = "\u221E";

    /**
     * A String containing the UTF-8 character for a shuffle button.
     */
    public static final String SHUFFLE_EMOJI = "\uD83D\uDD00";

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private FormatUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Formats the given duration into a String with the given Time.
     * @param duration The duration to format.
     * @return A String containing the formatted duration.
     */
    public static String formatTime(long duration)
    {
        if(duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = Math.round(duration/1000.0);
        long hours = seconds/(60*60);
        seconds %= 60*60;
        long minutes = seconds/60;
        seconds %= 60;
        return (hours>0 ? hours+":" : "") + (minutes<10 ? "0"+minutes : minutes) + ":" + (seconds<10 ? "0"+seconds : seconds);
    }

    /**
     * Get the current Status of the {@link AudioPlayer} as Emoji.
     * @param audioPlayer The {@link AudioPlayer} to get the Status from.
     * @return A String containing the current Status of the {@link AudioPlayer} as Emoji.
     */
    public static String getStatusEmoji(AudioPlayer audioPlayer) {
        return audioPlayer.isPaused() ? PAUSE_EMOJI : PLAY_EMOJI;
    }

    /**
     * Build a progressbar by the percentage.
     * @param percent The percentage of the progressbar.
     * @return A String containing the progressbar.
     */
    public static String progressBar(double percent)
    {
        StringBuilder str = new StringBuilder();
        for(int i=0; i<12; i++)
            if(i == (int)(percent*12))
                str.append("\uD83D\uDD18"); // 🔘
            else
                str.append("▬");
        return str.toString();
    }

    /**
     * Get the Volume Emoji by the volume
     * @param volume The volume to get the Emoji from.
     * @return A String containing the Volume Emoji.
     */
    public static String volumeIcon(int volume)
    {
        if(volume == 0)
            return "\uD83D\uDD07"; // 🔇
        if(volume < 30)
            return "\uD83D\uDD08"; // 🔈
        if(volume < 70)
            return "\uD83D\uDD09"; // 🔉
        return "\uD83D\uDD0A";     // 🔊
    }

    /**
     * Get a String of the TextChannels that match the query
     * @param list The list of the TextChannels.
     * @param query The query to match.
     * @return A String of the TextChannels that match the query.
     */
    public static String listOfTChannels(List<TextChannel> list, String query)
    {
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");
        for(int i=0; i<6 && i<list.size(); i++)
            out.append("\n - ").append(list.get(i).getName()).append(" (<#").append(list.get(i).getId()).append(">)");
        if(list.size()>6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }

    /**
     * Get a String of the VoiceChannels that match the query
     * @param list The list of the VoiceChannels.
     * @param query The query to match.
     * @return A String of the VoiceChannels that match the query.
     */
    public static String listOfVChannels(List<VoiceChannel> list, String query)
    {
        StringBuilder out = new StringBuilder(" Multiple voice channels found matching \"" + query + "\":");
        for(int i=0; i<6 && i<list.size(); i++)
            out.append("\n - ").append(list.get(i).getAsMention()).append(" (ID:").append(list.get(i).getId()).append(")");
        if(list.size()>6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }

    /**
     * Get a String of the Roles that match the query
     * @param list The list of the Roles.
     * @param query The query to match.
     * @return A String of the Roles that match the query.
     */
    public static String listOfRoles(List<Role> list, String query)
    {
        StringBuilder out = new StringBuilder(" Multiple text roles found matching \"" + query + "\":");
        for(int i=0; i<6 && i<list.size(); i++)
            out.append("\n - ").append(list.get(i).getName()).append(" (ID:").append(list.get(i).getId()).append(")");
        if(list.size()>6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }

    /**
     * Filter out @everyone and @here mentions from the given String.
     * @param input The String to filter.
     * @return A String without @everyone and @here mentions.
     */
    public static String filter(String input)
    {
        return input.replace("\u202E","")
                .replace("@everyone", "@\u0435veryone") // cyrillic letter e
                .replace("@here", "@h\u0435re") // cyrillic letter e
                .trim();
    }

    /**
     * Check if the given string is an url.
     * @param input The string to check.
     * @return True if the string is an url.
     */
    public static boolean isUrl(String input) {
        try {
            new URL(input);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}