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

import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * From https://github.com/jagrosh/MusicBot
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class FormatUtil {

    public static final String PLAY_EMOJI  = "\u25B6"; // ▶
    public static final String PAUSE_EMOJI = "\u23F8"; // ⏸
    public static final String STOP_EMOJI  = "\u23F9"; // ⏹

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private FormatUtil() {
        throw new IllegalStateException("Utility class");
    }

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

    public static String getStatusEmoji(AudioPlayer audioPlayer) {
        return audioPlayer.isPaused() ? PAUSE_EMOJI : PLAY_EMOJI;
    }

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
    
    public static String listOfTChannels(List<TextChannel> list, String query)
    {
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");
        for(int i=0; i<6 && i<list.size(); i++)
            out.append("\n - ").append(list.get(i).getName()).append(" (<#").append(list.get(i).getId()).append(">)");
        if(list.size()>6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }
    
    public static String listOfVChannels(List<VoiceChannel> list, String query)
    {
        StringBuilder out = new StringBuilder(" Multiple voice channels found matching \"" + query + "\":");
        for(int i=0; i<6 && i<list.size(); i++)
            out.append("\n - ").append(list.get(i).getAsMention()).append(" (ID:").append(list.get(i).getId()).append(")");
        if(list.size()>6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }
    
    public static String listOfRoles(List<Role> list, String query)
    {
        StringBuilder out = new StringBuilder(" Multiple text roles found matching \"" + query + "\":");
        for(int i=0; i<6 && i<list.size(); i++)
            out.append("\n - ").append(list.get(i).getName()).append(" (ID:").append(list.get(i).getId()).append(")");
        if(list.size()>6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }
    
    public static String filter(String input)
    {
        return input.replace("\u202E","")
                .replace("@everyone", "@\u0435veryone") // cyrillic letter e
                .replace("@here", "@h\u0435re") // cyrillic letter e
                .trim();
    }
}