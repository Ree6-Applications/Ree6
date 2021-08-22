package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
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
        super("play", "Play a song!", Category.MUSIC, new CommandData("play", "Play a song!").addOptions(new OptionData(OptionType.STRING, "name", "The YouTube URL, Song Name or the Spotify URL you want to play!").setRequired(true)));
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
            if (Main.musikWorker.isConnectedMember(sender, m.getGuild())) {
                if (ArrayUtil.botJoin.containsKey(m.getGuild())) {
                    ArrayUtil.botJoin.remove(m.getGuild());
                }
                ArrayUtil.botJoin.put(m.getGuild(), sender);
            }

            if(isUrl(args[0])) {
                boolean isspotify = false;
                ArrayList<String> spotiftrackinfos = null;

                if (args[0].contains("spotify")) {
                    try {
                        spotiftrackinfos = new SpotifyAPIHandler().convert(args[0]);
                        isspotify = true;
                    } catch (Exception ignored) {

                    }
                }

                if (!isspotify) {
                    Main.musikWorker.loadAndPlay(m, args[0]);
                } else {
                    ArrayList<String> loadfailed = new ArrayList<>();
                    boolean b = false;
                    for (String search : spotiftrackinfos) {
                        String ytresult = new YouTubeAPIHandler().searchYoutube(search);

                        if (ytresult == null) {
                            loadfailed.add(search);
                        } else {
                            if (!b) {
                                Main.musikWorker.loadAndPlay(m, ytresult);
                                b = true;
                            } else {
                                Main.musikWorker.loadAndPlaySilence(m, ytresult);
                            }
                        }
                    }

                    if (!loadfailed.isEmpty()) {
                        EmbedBuilder em = new EmbedBuilder();
                        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        em.setTitle("Music Player!");
                        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        em.setColor(Color.GREEN);
                        em.setDescription("We couldn't find " + loadfailed.size() + " Songs!");
                        em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
                        sendMessage(em, 5, m);
                    }
                }
            } else {
                StringBuilder search = new StringBuilder();

                for(String i : args) {
                    search.append(i).append(" ");
                }

                String ytresult = new YouTubeAPIHandler().searchYoutube(search.toString());

                if(ytresult == null) {
                    EmbedBuilder em = new EmbedBuilder();
                    em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setTitle("Music Player!");
                    em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setColor(Color.GREEN);
                    em.setDescription("A Song with the Name " + search + " couldn't be found!");
                    em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
                    sendMessage(em, 5, m);
                } else {
                    Main.musikWorker.loadAndPlay(m, ytresult);
                }
            }
        }
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
