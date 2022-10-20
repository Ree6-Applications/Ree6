package de.presti.ree6.events;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.Suggestions;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.entities.Tickets;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.entities.webhook.Webhook;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.ree6.utils.data.Data;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.jetbrains.annotations.NotNull;
import twitter4j.TwitterException;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

//TODO:: translate

/**
 * EventHandler for Menu Events.
 */
public class MenuEvents extends ListenerAdapter {

    /**
     * @inheritDoc
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);

        switch (event.getComponentId()) {
            case "re_suggestion" -> {
                Modal.Builder builder = Modal.create("re_suggestion_modal", "Suggestion");
                builder.addActionRow(TextInput.create("re_suggestion_text", "Suggestion", TextInputStyle.PARAGRAPH).setRequired(true).setMaxLength(2042).setMinLength(16).build());
                event.replyModal(builder.build()).queue();
            }

            case "re_ticket_open" -> {
                event.deferReply(true).queue();
                Tickets tickets = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new Tickets(), "SELECT * FROM Tickets WHERE GUILDID=:gid", Map.of("gid", event.getGuild().getId()));

                if (tickets != null) {
                    Category category = event.getGuild().getCategoryById(tickets.getTicketCategory());

                    if (category != null) {
                        if (category.getTextChannels().stream().anyMatch(c -> c.getName().contains(event.getUser().getName()))) {
                            event.getHook().sendMessage("You already have a ticket open!").queue();
                            return;
                        }

                        category.createTextChannel("ticket-" + event.getUser().getName())
                                .syncPermissionOverrides()
                                .addPermissionOverride(event.getMember(), List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS), List.of())
                                .queue(channel -> {
                                    MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                                    messageCreateBuilder.setEmbeds(new EmbedBuilder().setTitle("Ticket").setDescription("Welcome to your Ticket!").setThumbnail(event.getMember().getEffectiveAvatarUrl()).setColor(Color.GREEN).setTimestamp(Instant.now()).build());
                                    messageCreateBuilder.addActionRow(Button.primary("re_ticket_close", "Close the Ticket"));
                                    Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), channel);
                                    event.getHook().sendMessage("We opened a Ticket for you! Check it out " + channel.getAsMention()).queue();
                                });
                        tickets.setTicketCount(tickets.getTicketCount() + 1);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(tickets);
                    } else {
                        event.getHook().sendMessage("The Ticket Category is not set!").queue();
                    }
                }
            }

            case "re_ticket_close" -> {
                event.deferReply(true).queue();

                Tickets tickets = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new Tickets(), "SELECT * FROM Tickets WHERE GUILDID=:gid", Map.of("gid", event.getGuild().getId()));

                if (tickets != null) {
                    if (event.getGuildChannel().asTextChannel().getParentCategory().getName().startsWith("Archive")) {
                        event.getHook().sendMessage("This Ticket is already archived?").queue();
                        return;
                    }

                    Category category = event.getGuild().getCategoryById(tickets.getArchiveCategory());

                    if (category != null) {
                        if (category.getTextChannels().size() == 50) {
                            event.getGuild().createCategory("Archive-" + event.getGuild().getCategories().stream().filter(c -> c.getName().startsWith("Archive-")).count() + 1)
                                    .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue(c -> {
                                tickets.setArchiveCategory(c.getIdLong());
                                Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(tickets);
                                event.getGuildChannel().asTextChannel().getManager().setParent(c).sync().queue();
                            });
                        } else {
                            event.getGuildChannel().asTextChannel().getManager().setParent(category).sync().queue();
                        }

                        event.getHook().sendMessage("The Ticket was closed!").queue();
                    } else {
                        event.getHook().sendMessage("The Ticket Category is not set!").queue();
                    }
                }
            }
        }
    }


    /**
     * @inheritDoc
     */
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        super.onModalInteraction(event);


        switch (event.getModalId()) {
            case "re_suggestion_modal" -> {
                Suggestions suggestions = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new Suggestions(), "SELECT * FROM Suggestions WHERE guildId=:gid", Map.of("gid", event.getGuild().getId()));

                event.deferReply(true).queue();

                if (suggestions != null) {
                    MessageChannel messageChannel = (MessageChannel) event.getGuild().getGuildChannelById(suggestions.getChannelId());

                    if (messageChannel == null) return;

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Suggestion");
                    embedBuilder.setColor(Color.ORANGE);
                    embedBuilder.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                    embedBuilder.setDescription("```" + event.getValue("re_suggestion_text").getAsString() + "```");
                    embedBuilder.setFooter("Suggestion by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl());
                    embedBuilder.setTimestamp(Instant.now());
                    Main.getInstance().getCommandManager().sendMessage(embedBuilder, messageChannel);
                    Main.getInstance().getCommandManager().sendMessage("Suggestion sent!", null, event.getInteraction().getHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Looks like the Suggestion-System is not set up right?", null, event.getInteraction().getHook());
                }
            }

            case "statisticsSetupTwitchModal" -> {
                ModalMapping modalMapping = event.getValue("twitchChannelName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String twitchUsername = modalMapping.getAsString();

                java.util.List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                String channelId = Main.getInstance().getNotifier().getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(twitchUsername)).execute().getUsers().get(0).getId();
                event.getGuild().createVoiceChannel("Twitch Follower: " + Main.getInstance().getNotifier().getTwitchClient().getHelix().getFollowers(null, null, channelId, null, 20).execute().getTotal(), category).queue(voiceChannel -> {
                    ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
                    if (channelStats != null) {

                        if (channelStats.getTwitchFollowerChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getTwitchFollowerChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setTwitchFollowerChannelId(voiceChannel.getId());
                        channelStats.setTwitchFollowerChannelUsername(twitchUsername);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(channelStats);
                        Main.getInstance().getNotifier().registerTwitchChannel(twitchUsername);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                twitchUsername,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerTwitchChannel(twitchUsername);
                    }
                });

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Twitch statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupYouTubeModal" -> {
                ModalMapping modalMapping = event.getValue("youtubeChannelName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String youtubeChannelName = modalMapping.getAsString();

                java.util.List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for YouTube statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                com.google.api.services.youtube.model.Channel youTubeChannel;
                try {
                    if (YouTubeAPIHandler.getInstance().isValidChannelId(youtubeChannelName)) {
                        youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelById(youtubeChannelName, "statistics");
                    } else {
                        youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelBySearch(youtubeChannelName, "statistics");
                    }
                } catch (IOException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Channel data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                if (youTubeChannel == null) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("We could not find the given channel! You sure the name/id is correct?");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("YouTube Subscribers: " + (youTubeChannel.getStatistics().getHiddenSubscriberCount() ? "HIDDEN" : youTubeChannel.getStatistics().getSubscriberCount()), category).queue(voiceChannel -> {
                    ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
                    if (channelStats != null) {

                        if (channelStats.getYoutubeSubscribersChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getYoutubeSubscribersChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setYoutubeSubscribersChannelId(voiceChannel.getId());
                        channelStats.setYoutubeSubscribersChannelUsername(youtubeChannelName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(channelStats);
                        Main.getInstance().getNotifier().registerYouTubeChannel(youtubeChannelName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                youtubeChannelName,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerYouTubeChannel(youtubeChannelName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for YouTube statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupRedditModal" -> {
                ModalMapping modalMapping = event.getValue("subredditName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String subredditName = modalMapping.getAsString();

                java.util.List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Reddit statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                RedditSubreddit subreddit;
                try {
                    subreddit = Main.getInstance().getNotifier().getSubreddit(subredditName);
                } catch (IOException | InterruptedException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Subreddit data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("Subreddit Members: " + subreddit.getActiveUserCount(), category).queue(voiceChannel -> {
                    ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
                    if (channelStats != null) {

                        if (channelStats.getSubredditMemberChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getSubredditMemberChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setSubredditMemberChannelId(voiceChannel.getId());
                        channelStats.setSubredditMemberChannelSubredditName(subredditName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(channelStats);
                        Main.getInstance().getNotifier().registerSubreddit(subredditName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                subredditName);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerSubreddit(subredditName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Reddit statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupTwitterModal" -> {
                ModalMapping modalMapping = event.getValue("twitterName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String twitterName = modalMapping.getAsString();

                java.util.List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Twitter statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                twitter4j.User twitterUser;
                try {
                    twitterUser = Main.getInstance().getNotifier().getTwitterClient().showUser(twitterName);
                } catch (TwitterException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Twitter User data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("Twitter Follower: " + twitterUser.getFollowersCount(), category).queue(voiceChannel -> {
                    ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
                    if (channelStats != null) {

                        if (channelStats.getTwitterFollowerChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getTwitterFollowerChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setTwitterFollowerChannelId(voiceChannel.getId());
                        channelStats.setTwitterFollowerChannelUsername(twitterName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(channelStats);
                        Main.getInstance().getNotifier().registerTwitterUser(twitterName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                twitterName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerTwitterUser(twitterName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Twitter statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupInstagramModal" -> {
                ModalMapping modalMapping = event.getValue("instagramName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String instagramName = modalMapping.getAsString();

                java.util.List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Instagram statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                com.github.instagram4j.instagram4j.models.user.User instagramUser;
                try {
                    instagramUser = Main.getInstance().getNotifier().getInstagramClient().getActions().users().findByUsername(instagramName).get().getUser();
                } catch (ExecutionException | InterruptedException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Instagram User data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("Instagram Follower: " + instagramUser.getFollower_count(), category).queue(voiceChannel -> {
                    ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
                    if (channelStats != null) {

                        if (channelStats.getInstagramFollowerChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getInstagramFollowerChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setInstagramFollowerChannelId(voiceChannel.getId());
                        channelStats.setInstagramFollowerChannelUsername(instagramName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(channelStats);
                        Main.getInstance().getNotifier().registerInstagramUser(instagramName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                instagramName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerInstagramUser(instagramName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Instagram statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            default -> {
                event.deferEdit().setEmbeds(new EmbedBuilder()
                        .setTitle("Unknown Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.RED)
                        .setDescription("There was an error while resolving the modal!")
                        .build()).setComponents(new ArrayList<>()).queue();
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        super.onSelectMenuInteraction(event);

        if (event.getInteraction().getComponent().getId() == null ||
                event.getGuild() == null)
            return;

        if (event.getMessage().getEmbeds().isEmpty() ||
                event.getMessage().getEmbeds().get(0) == null ||
                event.getInteraction().getSelectedOptions().isEmpty())
            return;

        if (event.getInteraction().getValues().isEmpty())
            return;

        switch (event.getInteraction().getComponent().getId()) {
            case "setupActionMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                java.util.List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {
                    case "log" -> {
                        optionList.add(SelectOption.of("Setup", "logSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "logDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Audit-Logging which provides all the Information over and Webhook into the Channel of your desire! " + "But ours is not the same as the default Auditions, ours gives your the ability to set what you want to be logged and what not! " + "We also allow you to log Voice Events!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupLogMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "welcome" -> {
                        optionList.add(SelectOption.of("Setup", "welcomeSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "welcomeDelete"));

                        optionList.add(SelectOption.of("Set Image", "welcomeImage"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Welcome-Messages!\nYou can choice the Welcome-Channel by your own and even configure the Message!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupWelcomeMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "autorole" -> {
                        embedBuilder.setDescription("You can set up our own Autorole-System!\nYou can select Roles that Users should get upon joining the Server!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(Button.link("https://cp.ree6.de", "Webinterface")).queue();
                    }

                    case "tempvoice" -> {
                        optionList.add(SelectOption.of("Setup", "tempVoiceSetup"));
                        if (Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", event.getGuild().getId())) != null)
                            optionList.add(SelectOption.of("Delete", "tempVoiceDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up your own Temporal Voicechannel!\nBy setting up Temporal Voicechannel on a specific channel which will be used to create a new Voicechannel when ever someones joins into it!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupTempVoiceMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "statistics" -> {
                        optionList.add(SelectOption.of("Setup Member Statistics", "statisticsSetupMember"));
                        optionList.add(SelectOption.of("Setup Twitch Statistics", "statisticsSetupTwitch"));
                        optionList.add(SelectOption.of("Setup YouTube Statistics", "statisticsSetupYouTube"));
                        optionList.add(SelectOption.of("Setup Reddit Statistics", "statisticsSetupReddit"));
                        optionList.add(SelectOption.of("Setup Twitter Statistics", "statisticsSetupTwitter"));
                        optionList.add(SelectOption.of("Setup Instagram Statistics", "statisticsSetupInstagram"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up your own Statistic-channels!\nBy setting up Statistic-channels Ree6 will create new channels for each Statistic-Type that you setup!\nIf you want to get rid of a Statistic-Channel, just delete it!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupStatisticsMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "tickets" -> {
                        optionList.add(SelectOption.of("Setup", "ticketsSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new Tickets(), "SELECT * FROM Tickets WHERE GUILDID=:gid", Map.of("gid", event.getGuild().getId())) != null)
                            optionList.add(SelectOption.of("Delete", "ticketsDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up your own Ticket-System!\nBy setting up a specific channel as Ticket-Channel, Ree6 will create a new Ticket-Channel for each Ticket that you create!\nAfter Ticket closing those tickets will be moved to a archive category!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupTicketsMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    default -> {
                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupStatisticsMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "statisticsSetupMember" -> {
                        embedBuilder.setDescription("Successfully setup the statistics channels for Member statistics!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        java.util.List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                        Category category;

                        if (categories.isEmpty()) {
                            category = event.getGuild().createCategory("Statistics").complete();
                        } else {
                            category = categories.get(0);
                        }

                        event.getGuild().loadMembers().onSuccess(members -> event.getGuild().createVoiceChannel("Overall Members: " + event.getGuild().getMemberCount(), category).queue(voiceChannel -> {
                            voiceChannel.getManager().setUserLimit(0).queue();
                            event.getGuild().createVoiceChannel("Real Members: " + members.stream().filter(member -> !member.getUser().isBot()).count(), category).queue(voiceChannel1 -> {
                                voiceChannel1.getManager().setUserLimit(0).queue();
                                event.getGuild().createVoiceChannel("Bot Members: " + members.stream().filter(member -> member.getUser().isBot()).count(), category).queue(voiceChannel2 -> {
                                    voiceChannel2.getManager().setUserLimit(0).queue();
                                    ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
                                    if (channelStats != null) {
                                        if (channelStats.getMemberStatsChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getMemberStatsChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }
                                        if (channelStats.getRealMemberStatsChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getRealMemberStatsChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }
                                        if (channelStats.getBotMemberStatsChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getBotMemberStatsChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }
                                        channelStats.setMemberStatsChannelId(voiceChannel.getId());
                                        channelStats.setRealMemberStatsChannelId(voiceChannel1.getId());
                                        channelStats.setBotMemberStatsChannelId(voiceChannel2.getId());
                                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(channelStats);
                                    } else {
                                        channelStats = new ChannelStats(event.getGuild().getId(),
                                                voiceChannel.getId(),
                                                voiceChannel1.getId(),
                                                voiceChannel2.getId(),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null);
                                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                                    }
                                });
                            });
                        }));
                    }

                    case "statisticsSetupTwitch" -> {
                        TextInput input = TextInput.create("twitchChannelName", "Twitch Channel Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Twitch Channel name here!").build();

                        Modal modal = Modal.create("statisticsSetupTwitchModal", "Twitch Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupYouTube" -> {
                        TextInput input = TextInput.create("youtubeChannelName", "YouTube Channel Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the YouTube Channel name here!").build();

                        Modal modal = Modal.create("statisticsSetupYouTubeModal", "YouTube Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupReddit" -> {
                        TextInput input = TextInput.create("subredditName", "Subreddit Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Subreddit name here!").build();

                        Modal modal = Modal.create("statisticsSetupRedditModal", "Reddit Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupTwitter" -> {
                        TextInput input = TextInput.create("twitterName", "Twitter Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Twitter name here!").build();

                        Modal modal = Modal.create("statisticsSetupTwitterModal", "Twitter Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupInstagram" -> {
                        TextInput input = TextInput.create("instagramName", "Instagram Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Instagram name here!").build();

                        Modal modal = Modal.create("statisticsSetupInstagramModal", "Instagram Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupTickets" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                MessageChannel messageChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (messageChannel != null) {
                    Tickets tickets = new Tickets();
                    tickets.setChannelId(messageChannel.getIdLong());
                    tickets.setGuildId(event.getGuild().getIdLong());
                    event.getGuild().createCategory("Archive").addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue(category -> {
                        tickets.setArchiveCategory(category.getIdLong());

                        event.getGuild().createCategory("Tickets").addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue(category1 -> {
                            tickets.setTicketCategory(category1.getIdLong());
                            Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(tickets);

                            MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                            messageCreateBuilder.setEmbeds(new EmbedBuilder()
                                    .setTitle("Open a Ticket!")
                                    .setDescription("By clicking on the Button below you can open a Ticket!")
                                    .setColor(0x55ff00)
                                    .setThumbnail(event.getGuild().getIconUrl())
                                    .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                                    .build());
                            messageCreateBuilder.setActionRow(Button.of(ButtonStyle.PRIMARY, "re_ticket_open", "Open a Ticket!", Emoji.fromUnicode("U+1F4E9")));
                            Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), messageChannel);
                        });
                    });
                    embedBuilder.setDescription("Successfully changed the Ticket System, nice work!");
                    embedBuilder.setColor(Color.GREEN);
                    event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }
            }

            case "setupTicketsMenu" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                java.util.List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "ticketsSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want as Ticket-Channel?\nWe will send a Messsage that allows users to create a Ticket.");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupTickets", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    case "ticketsDelete" -> {
                        Tickets tickets = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new Tickets(), "SELECT * FROM Tickets WHERE GUILDID=:gid", Map.of("gid", event.getGuild().getId()));

                        if (tickets != null) {
                            embedBuilder.setDescription("Successfully deleted the Ticket-Channel, nice work!");
                            embedBuilder.setColor(Color.GREEN);
                            event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                            Main.getInstance().getSqlConnector().getSqlWorker().deleteEntity(tickets);
                        }
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupTempVoicechannel" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getInteraction().getValues().get(0));

                if (voiceChannel != null) {
                    Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(new TemporalVoicechannel(event.getGuild().getId(), voiceChannel.getId()));
                    embedBuilder.setDescription("Successfully changed the Temporal Voicechannel, nice work!");
                    embedBuilder.setColor(Color.GREEN);
                    event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }
            }

            case "setupTempVoiceMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                java.util.List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "tempVoiceSetup" -> {
                        for (VoiceChannel channel : event.getGuild().getVoiceChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Temporal-Voicechannel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupTempVoicechannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    case "tempVoiceDelete" -> {
                        TemporalVoicechannel temporalVoicechannel = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));

                        if (temporalVoicechannel != null) {
                            embedBuilder.setDescription("Successfully deleted the Temporal-Voicechannel, nice work!");
                            embedBuilder.setColor(Color.GREEN);
                            event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                            Main.getInstance().getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel);
                        }
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupLogMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                java.util.List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "logSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Logging-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupLogChannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    case "logDelete" -> {
                        Webhook webhook = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());

                        if (webhook != null) {
                            event.getJDA().retrieveWebhookById(webhook.getChannelId()).queue(webhook1 -> {
                                webhook1.delete().queue();
                                embedBuilder.setDescription("Successfully deleted the Log Channel, nice work!");
                                embedBuilder.setColor(Color.GREEN);
                                event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                                Main.getInstance().getSqlConnector().getSqlWorker().deleteEntity(webhook);
                            });
                        }
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupLogChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-Logs").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setLogWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the Logging Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }

            }

            case "setupWelcomeMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                java.util.List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "welcomeSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Welcome-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupWelcomeChannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    case "welcomeImage" -> {
                        embedBuilder.setDescription("Use the following Command with a Image as attachment: `" + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "chatprefix").getStringValue() + "setup joinImage`");
                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new ArrayList<>()).queue();
                    }

                    case "welcomeDelete" -> {
                        Webhook webhook = Main.getInstance().getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getId());

                        if (webhook != null) {
                            event.getJDA().retrieveWebhookById(webhook.getChannelId()).queue(webhook1 -> {
                                webhook1.delete().queue();
                                embedBuilder.setDescription("Successfully deleted the Welcome Channel, nice work!");
                                embedBuilder.setColor(Color.GREEN);
                                event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                                Main.getInstance().getSqlConnector().getSqlWorker().deleteEntity(webhook);
                            });
                        }
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupWelcomeChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-Welcome").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setWelcomeWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the Welcome-Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }

            }

            default -> {
                if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null) return;

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                event.editMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    /**
     * Called when the default choices should be sent.
     *
     * @param event The InteractionEvent of the SelectMenu.
     */
    public void sendDefaultChoice(SelectMenuInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

        List<SelectOption> optionList = new ArrayList<>();
        optionList.add(SelectOption.of("Audit-Logging", "log"));
        optionList.add(SelectOption.of("Welcome-channel", "welcome"));
        optionList.add(SelectOption.of("Autorole", "autorole"));
        optionList.add(SelectOption.of("Temporal-Voice", "tempvoice"));
        optionList.add(SelectOption.of("Statistics", "statistics"));
        optionList.add(SelectOption.of("Ticket-System", "tickets"));

        embedBuilder.setDescription("Which configuration do you want to check out?");

        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList)).queue();
    }

    /**
     * Checks if the user has the required Permissions to use the Command.
     *
     * @param member  The Member who should be checked.
     * @param channel The Channel used.
     * @return True if the user has the required Permissions, false if not.
     */
    private boolean checkPerms(Member member, MessageChannel channel) {
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("You do not have enough Permissions").queue();
            return true;
        }

        if (!member.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            channel.sendMessage("I do not have enough Permissions to create Webhooks").queue();
            return true;
        }

        return false;
    }
}
