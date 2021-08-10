package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.music.MusikWorker;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.SpotifyAPIHandler;
import de.presti.ree6.utils.YouTubeAPIHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Play extends Command {

    public Play() {
        super("play", "Play a song!", Category.MUSIC, new CommandData("play", "Play a song!").addOptions(new OptionData(OptionType.STRING, "url/name", "The YouTube URL, Song Name or the Spotify URL you want to play!").setRequired(true)));
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
                        spotiftrackinfos = new SpotifyAPIHandler().convert(args[0]);
                        isspotify = true;
                    } catch (Exception ex) {

                    }
                }

                if (!isspotify) {
                    MusikWorker.loadAndPlay(m, args[0]);
                } else {
                    ArrayList<String> loadfailed = new ArrayList<>();
                    boolean b = false;
                    for (String search : spotiftrackinfos) {
                        String ytresult = new YouTubeAPIHandler().searchYoutube(search);

                        if (ytresult == null) {
                            loadfailed.add(search);
                        } else {
                            if (!b) {
                                MusikWorker.loadAndPlay(m, ytresult);
                                b = true;
                            } else {
                                MusikWorker.loadAndPlaySilence(m, ytresult);
                            }
                        }
                    }

                    if (!loadfailed.isEmpty()) {
                        EmbedBuilder em = new EmbedBuilder();
                        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        em.setTitle("Music Player!");
                        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        em.setColor(Color.GREEN);
                        em.setDescription("We couldnt find " + loadfailed.size() + " Songs!");
                        em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
                        sendMessage(em, 5, m);
                    }
                }
            } else {
                String search = "";

                for(String i : args) {
                    search += i + " ";
                }

                String ytresult = new YouTubeAPIHandler().searchYoutube(search);

                if(ytresult == null) {
                    EmbedBuilder em = new EmbedBuilder();
                    em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setTitle("Music Player!");
                    em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setColor(Color.GREEN);
                    em.setDescription("A Song with the Name " + search + " couldnt be found!");
                    em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
                    sendMessage(em, 5, m);
                } else {
                    MusikWorker.loadAndPlay(m, ytresult);
                }
            }
        }
        deleteMessage(messageSelf);
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
