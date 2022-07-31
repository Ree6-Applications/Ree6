package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.ree6.utils.others.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Play a Song.
 */
@Command(name = "play", description = "Play a new Song or add a Song to the current Queue.", category = Category.MUSIC)
public class Play implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        if (commandEvent.isSlashCommand()) {

            OptionMapping valueOption = commandEvent.getSlashCommandInteractionEvent().getOption("name");

            if (valueOption != null) {
                playSong(valueOption.getAsString(), commandEvent);
            } else {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                        commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Usage: " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "play (Url)");
                em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
                Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }

        } else {
            if (commandEvent.getArguments().length < 1) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                        commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Usage: " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "play (Url)");
                em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
                Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                playSong(commandEvent.getArguments()[0], commandEvent);
            }
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("play", "Play a song!").addOptions(new OptionData(OptionType.STRING, "name", "The YouTube URL, Song Name or the Spotify URL you want to play!").setRequired(true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"p", "music"};
    }

    /**
     * Play a specific song.
     * @param value The song name or url.
     * @param commandEvent The command event.
     */
    public void playSong(String value, CommandEvent commandEvent) {
        if (isUrl(value)) {
            boolean isspotify = false;
            ArrayList<String> spotiftrackinfos = null;

            if (value.contains("spotify")) {
                try {
                    spotiftrackinfos = new SpotifyAPIHandler().convert(value);
                    isspotify = true;
                } catch (Exception ignored) {

                }
            }

            if (!isspotify) {
                Main.getInstance().getMusicWorker().loadAndPlay(commandEvent.getChannel(), Objects.requireNonNull(commandEvent.getMember().getVoiceState()).getChannel(), value, commandEvent.getInteractionHook());
            } else {
                ArrayList<String> loadFailed = new ArrayList<>();

                boolean tempBoolean = false;

                for (String search : spotiftrackinfos) {
                    String result = null;
                    try {
                        result = YouTubeAPIHandler.getInstance().searchYoutube(search);
                    } catch (Exception exception) {
                        Main.getInstance().getLogger().error("Error while searching for " + search + " on YouTube", exception);
                    }

                    if (result == null) {
                        loadFailed.add(search);
                    } else {
                        if (!tempBoolean) {
                            Main.getInstance().getMusicWorker().loadAndPlay(commandEvent.getChannel(), Objects.requireNonNull(commandEvent.getMember().getVoiceState()).getChannel(), result, commandEvent.getInteractionHook());
                            tempBoolean = true;
                        } else {
                            Main.getInstance().getMusicWorker().loadAndPlaySilence(commandEvent.getChannel(), Objects.requireNonNull(commandEvent.getMember().getVoiceState()).getChannel(), result, commandEvent.getInteractionHook());
                        }
                    }
                }

                if (!loadFailed.isEmpty()) {
                    EmbedBuilder em = new EmbedBuilder();
                    em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE, commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                    em.setTitle("Music Player!");
                    em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                    em.setColor(Color.GREEN);
                    em.setDescription("We couldn't find ``" + loadFailed.size() + "`` Songs!");
                    em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
                    Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            StringBuilder search = new StringBuilder();

            if (commandEvent.isSlashCommand()) {
                search.append(value);
            } else {
                for (String i : commandEvent.getArguments()) {
                    search.append(i).append(" ");
                }
            }

            String ytResult;

            try {
                ytResult = YouTubeAPIHandler.getInstance().searchYoutube(search.toString());
            } catch (Exception exception) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE, commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setColor(Color.RED);
                em.setDescription("We had an Issue searching for the Song, please try again in 15 minutes!");
                em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
                Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                Main.getInstance().getLogger().error("Error while searching for " + search + " on YouTube", exception);
                return;
            }

            if (ytResult == null) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE, commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
                em.setColor(Color.YELLOW);
                em.setDescription("A Song with the Name ``" + FormatUtil.filter(search.toString()) + "`` couldn't be found!");
                em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
                Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            } else {
                Main.getInstance().getMusicWorker().loadAndPlay(commandEvent.getChannel(), Objects.requireNonNull(commandEvent.getMember().getVoiceState()).getChannel(), ytResult, commandEvent.getInteractionHook());
            }
        }
    }

    /**
     * Check if the given string is an url.
     * @param input The string to check.
     * @return True if the string is an url.
     */
    private boolean isUrl(String input) {
        try {
            new URL(input);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
