package de.presti.ree6.commands.impl.music;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.music.MusikWorker;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.LinkConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Play extends Command {

    YouTube yt;

    public Play() {
        super("play", "Play a song!", Category.MUSIC);
        YouTube temp = null;

        try {
            temp = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("Ree6")
                    .build();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        yt = temp;
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (args.length < 1) {
            EmbedBuilder em = new EmbedBuilder();
            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("Usage: ree!play (Url)");
            em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
            sendMessage(em, 5, m);
        } else {
            if (MusikWorker.isConnectedMember(sender, m.getGuild())) {
                if (ArrayUtil.botjoin.containsKey(m.getGuild())) {
                    ArrayUtil.botjoin.remove(m.getGuild());
                }
                ArrayUtil.botjoin.put(m.getGuild(), sender);
            }

            if(isUrl(args[0])) {
                boolean isspotify = false;
                ArrayList<String> spotiftrackinfos = null;

                if (args[0].contains("spotify")) {
                    try {
                        spotiftrackinfos = LinkConverter.getInstance().convert(args[0]);
                        isspotify = true;
                    } catch (Exception ex) {

                    }
                }

                if (!isspotify) {
                    MusikWorker.loadAndPlay(m, args[0]);
                } else {
                    for(String search : spotiftrackinfos) {
                        String ytresult = searchYoutube(search);

                        if(ytresult == null) {
                            sendMessage("Coudln't find an results for " + search + "!", 5, m);
                        } else {
                            MusikWorker.loadAndPlay(m, ytresult);
                        }
                    }
                }
            } else {
                String search = "";

                for(String i : args) {
                    search += i + " ";
                }

                String ytresult = searchYoutube(search);

                if(ytresult == null) {
                    sendMessage("Coudln't find an results for " + search + "!", 5, m);
                } else {
                    MusikWorker.loadAndPlay(m, ytresult);
                }
            }
        }
        messageSelf.delete().queue();
    }

    private String searchYoutube (String search) {
        try {
            List<SearchResult> results = yt.search()
                    .list(Collections.singletonList("id,snippet"))
                    .setQ(search)
                    .setMaxResults(4L)
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setKey(Main.config.getConfig().getString("youtube.api.key"))
                    .execute()
                    .getItems();

            if(!results.isEmpty()) {
                String videoId = results.get(0).getId().getVideoId();


                return "https://www.youtube.com/watch?v=" + videoId;
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean isUrl(String input) {
        try {
            new URL(input);
            return true;
        }
        catch (MalformedURLException e){
            return false;
        }
    }
}
