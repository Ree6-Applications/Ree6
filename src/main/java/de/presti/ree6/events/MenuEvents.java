package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.commands.impl.mod.Setup;
import de.presti.ree6.language.Language;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.*;
import de.presti.ree6.sql.entities.roles.AutoRole;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.wrapper.entities.channel.ChannelResult;
import io.github.redouane59.twitter.dto.user.UserV2;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

        if (BotConfig.allowRecordingInChat() && event.getComponentId().startsWith("r_recordingDownload:") && event.getComponentId().contains(":")) {
            String[] split = event.getComponentId().split(":");

            if (split.length == 2) {
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Recording(), "FROM Recording WHERE identifier = :id AND guildId = :gid", Map.of("id", split[1], "gid", event.getGuild().getIdLong())).subscribe(recording -> {
                    MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
                    if (recording.isPresent()) {
                        messageEditBuilder.setEmbeds(new EmbedBuilder()
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.recording.inChat").block())
                                .setColor(Color.GREEN)
                                .setFooter(BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.recording.finished").block())
                                .build());
                        messageEditBuilder.setFiles(FileUpload.fromData(recording.get().getRecording(), "recording.wav"));
                        messageEditBuilder.setComponents(List.of());
                    } else {
                        messageEditBuilder.setEmbeds(new EmbedBuilder()
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.recording.notFound").block())
                                .setColor(Color.RED)
                                .setFooter(BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.error").block())
                                .build());
                        messageEditBuilder.setComponents(List.of());
                    }

                    event.editMessage(messageEditBuilder.build()).queue();
                });
            }
        }

        switch (event.getComponentId()) {
            case "re_feedback" -> {
                LanguageService.getByGuild(event.getGuild(), "label.feedback").subscribe(modalString -> {
                    Modal.Builder builder = Modal.create("re_feedback_modal", modalString);
                    builder.addActionRow(TextInput.create("re_feedback_text", modalString, TextInputStyle.PARAGRAPH).setRequired(true).setMaxLength(2042).setMinLength(16).build());
                    event.replyModal(builder.build()).queue();
                });
            }

            case "re_suggestion" -> {
                LanguageService.getByGuild(event.getGuild(), "label.suggestion").subscribe(modalString -> {
                    Modal.Builder builder = Modal.create("re_suggestion_modal", modalString);
                    builder.addActionRow(TextInput.create("re_suggestion_text", modalString, TextInputStyle.PARAGRAPH).setRequired(true).setMaxLength(2042).setMinLength(16).build());
                    event.replyModal(builder.build()).queue();
                });
            }

            case "re_ticket_open" -> {
                event.deferReply(true).queue();
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(tickets -> {
                    if (tickets.isPresent()) {
                        Tickets ticketEntity = tickets.get();
                        Category category = event.getGuild().getCategoryById(ticketEntity.getTicketCategory());

                        if (category != null) {
                            if (category.getTextChannels().stream().anyMatch(c -> c.getName().contains(event.getUser().getName()))) {
                                event.getHook().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.ticket.alreadyOpen").block()).queue();
                                return;
                            }

                            SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "message_ticket_open").subscribe(ticketMessage -> {
                                category.createTextChannel("ticket-" + event.getUser().getName())
                                        .setTopic(event.getUser().getId())
                                        .syncPermissionOverrides()
                                        .addPermissionOverride(event.getMember(), List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS), List.of())
                                        .queue(channel -> {
                                            MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                                            messageCreateBuilder.setEmbeds(new EmbedBuilder().setTitle(LanguageService.getByGuild(event.getGuild(), "label.ticket").block())
                                                    .setDescription(ticketMessage.get().getStringValue())
                                                    .setThumbnail(event.getMember().getEffectiveAvatarUrl()).setColor(Color.GREEN).setTimestamp(Instant.now()).build());
                                            messageCreateBuilder.addActionRow(Button.primary("re_ticket_close", LanguageService.getByGuild(event.getGuild(), "label.closeTicket").block()));
                                            Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), channel);
                                            event.getHook().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.ticket.created", channel.getAsMention()).block()).queue();
                                        });
                                ticketEntity.setTicketCount(ticketEntity.getTicketCount() + 1);
                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(ticketEntity).block();
                            });
                        } else {
                            event.getHook().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.ticket.categoryNotFound").block()).queue();
                        }
                    }
                });
            }

            case "re_ticket_close" -> {
                event.deferReply(true).queue();

                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(tickets -> {
                    if (tickets.isPresent()) {
                        Tickets ticketEntity = tickets.get();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(BotConfig.getBotName()).append(" Ticket transcript")
                                .append(" ")
                                .append(ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)))
                                .append("\n")
                                .append("\n");


                        for (Message message : event.getChannel().asTextChannel().getIterableHistory().reverse()) {
                            stringBuilder
                                    .append("[")
                                    .append(message.getTimeCreated().toZonedDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
                                    .append("]")
                                    .append(" ")
                                    .append(message.getAuthor().getName())
                                    .append(" ")
                                    .append("->")
                                    .append(" ")
                                    .append(message.getContentRaw());

                            if (!message.getAttachments().isEmpty()) {
                                for (Message.Attachment attachment : message.getAttachments()) {
                                    stringBuilder.append("\n").append(attachment.getUrl());
                                }
                            }

                            stringBuilder.append("\n");
                        }

                        // TODO:: translate and fix the date being shown as UTC+1 and instead use the current server region.

                        stringBuilder.append("\n").append("Closed by").append(" ").append(event.getUser().getName());

                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                        webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        webhookMessageBuilder.setUsername(BotConfig.getBotName() + "-Tickets");

                        WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                        webhookEmbedBuilder.setDescription("Here is the transcript of the ticket " + ticketEntity.getTicketCount() + "!");
                        webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        webhookEmbedBuilder.setColor(BotWorker.randomEmbedColor().getRGB());

                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());
                        webhookMessageBuilder.addFile(ticketEntity.getTicketCount() + "_transcript.txt", stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

                        WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), ticketEntity.getLogChannelId(), ticketEntity.getLogChannelWebhookToken(), false);

                        event.getHook().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.ticket.close").block()).queue();
                        event.getChannel().delete().delay(2, TimeUnit.SECONDS).queue();
                    }
                });
            }

            case "re_music_play" -> {
                event.deferReply(true).queue();
                GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild());

                if (guildMusicManager != null) {
                    if (guildMusicManager.getPlayer().isPaused()) {
                        guildMusicManager.getPlayer().setPaused(false);
                        EmbedBuilder em = new EmbedBuilder()
                                .setAuthor(event.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                                        event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl())
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.musicPlayer").block())
                                .setThumbnail(event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl())
                                .setColor(Color.GREEN)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.music.resume").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl());
                        Main.getInstance().getCommandManager().sendMessage(em, event.getChannel(), event.getHook());
                    } else {
                        EmbedBuilder em = new EmbedBuilder()
                                .setAuthor(event.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                                        event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl())
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.musicPlayer").block())
                                .setThumbnail(event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl())
                                .setColor(Color.GREEN)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.music.pause").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl());
                        Main.getInstance().getCommandManager().sendMessage(em, event.getChannel(), event.getHook());
                    }
                } else {
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.music.notConnected"),
                            event.getChannel(), event.getHook());
                }
            }

            case "re_music_pause" -> {
                event.deferReply(true).queue();
                GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild());

                if (guildMusicManager != null) {
                    guildMusicManager.getPlayer().setPaused(true);
                    EmbedBuilder em = new EmbedBuilder()
                            .setAuthor(event.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                                    event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.musicPlayer").block())
                            .setThumbnail(event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.music.pause").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl());
                    Main.getInstance().getCommandManager().sendMessage(em, event.getChannel(), event.getHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.music.notConnected"),
                            event.getChannel(), event.getHook());
                }
            }

            case "re_music_skip" -> {
                event.deferReply(true).queue();
                GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild());

                if (guildMusicManager != null) {
                    Main.getInstance().getMusicWorker().skipTrack(event.getGuildChannel(), event.getHook(), 1, false);
                } else {
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.music.notConnected"),
                            event.getChannel(), event.getHook());
                }
            }

            case "re_music_loop" -> {
                event.deferReply(true).queue();
                GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild());

                if (guildMusicManager != null) {
                    EmbedBuilder em = new EmbedBuilder();

                    em.setAuthor(event.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                            event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                    em.setTitle(LanguageService.getByGuild(event.getGuild(), "label.musicPlayer").block());
                    em.setThumbnail(event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                    em.setColor(Color.GREEN);
                    em.setDescription(Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild()).getScheduler().loop() ?
                            LanguageService.getByGuild(event.getGuild(), "message.music.loop.enabled").block() :
                            LanguageService.getByGuild(event.getGuild(), "message.music.loop.disabled").block());
                    em.setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl());

                    Main.getInstance().getCommandManager().sendMessage(em, event.getChannel(), event.getHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.music.notConnected"),
                            event.getChannel(), event.getHook());
                }
            }

            case "re_music_shuffle" -> {
                event.deferReply(true).queue();
                GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild());

                if (guildMusicManager != null) {
                    EmbedBuilder em = new EmbedBuilder();

                    guildMusicManager.getScheduler().shuffle();

                    em.setAuthor(event.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                            event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                    em.setTitle(LanguageService.getByGuild(event.getGuild(), "label.musicPlayer").block());
                    em.setThumbnail(event.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                    em.setColor(Color.GREEN);
                    em.setDescription(LanguageService.getByGuild(event.getGuild(), "message.music.shuffle").block());
                    em.setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl());

                    Main.getInstance().getCommandManager().sendMessage(em, event.getChannel(), event.getHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.music.notConnected"),
                            event.getChannel(), event.getHook());
                }
            }

            case "re_music_add" -> {
                Modal.Builder builder = Modal.create("re_music_add_modal", LanguageService.getByGuild(event.getGuild(), "label.queueAdd").block());
                builder.addActionRow(TextInput.create("re_music_add_modal_song", LanguageService.getByGuild(event.getGuild(), "label.song").block(), TextInputStyle.PARAGRAPH).setRequired(true).setMaxLength(512).setMinLength(4).build());
                event.replyModal(builder.build()).queue();
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
            case "re_rewards_modal" -> {
                event.deferReply(true).queue();
                LanguageService.getByGuild(event.getGuild(), "label.rewards").subscribe(title -> {
                    String blackJackString = event.getValue("re_rewards_BlackJackWin").getAsString();
                    String musicQuizWinString = event.getValue("re_rewards_MusicQuizWin").getAsString();
                    String musicQuizFeatureString = event.getValue("re_rewards_MusicQuizFeature").getAsString();
                    String musicQuizArtistString = event.getValue("re_rewards_MusicQuizArtist").getAsString();
                    String musicQuizTitleString = event.getValue("re_rewards_MusicQuizTitle").getAsString();

                    double blackJackAmount = 0, musicWinAmount = 0, musicFeatureAmount = 0, musicArtistAmount = 0, musicTitleAmount = 0;

                    try {
                        blackJackAmount = Double.parseDouble(blackJackString);
                        musicWinAmount = Double.parseDouble(musicQuizWinString);
                        musicFeatureAmount = Double.parseDouble(musicQuizFeatureString);
                        musicArtistAmount = Double.parseDouble(musicQuizArtistString);
                        musicTitleAmount = Double.parseDouble(musicQuizTitleString);
                    } catch (Exception exception) {
                        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                                .setTitle(title)
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.incorrectNumbers").block())
                                .setFooter(BotConfig.getAdvertisement(), event.getGuild().getIconUrl()), null, event.getInteraction().getHook());
                    }

                    SQLSession.getSqlConnector().getSqlWorker()
                            .setSetting(event.getGuild().getIdLong(), "configuration_rewards_blackjack_win", "Payment Amount on BlackJack win", blackJackAmount);

                    SQLSession.getSqlConnector().getSqlWorker()
                            .setSetting(event.getGuild().getIdLong(), "configuration_rewards_musicquiz_win", "Payment Amount on Music Quiz win", musicWinAmount);

                    SQLSession.getSqlConnector().getSqlWorker()
                            .setSetting(event.getGuild().getIdLong(), "configuration_rewards_musicquiz_feature", "Payment Amount on Music Quiz Feature guess", musicFeatureAmount);

                    SQLSession.getSqlConnector().getSqlWorker()
                            .setSetting(event.getGuild().getIdLong(), "configuration_rewards_musicquiz_artist", "Payment Amount on Music Quiz Artist guess", musicArtistAmount);

                    SQLSession.getSqlConnector().getSqlWorker()
                            .setSetting(event.getGuild().getIdLong(), "configuration_rewards_musicquiz_title", "Payment Amount on Music Quiz Title guess", musicTitleAmount);

                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setTitle(title)
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.rewards.success").block())
                            .setFooter(BotConfig.getAdvertisement(), event.getGuild().getIconUrl()), null, event.getInteraction().getHook());
                });

            }

            case "re_feedback_modal" -> {
                event.deferReply(true).queue();

                LanguageService.getByGuild(event.getGuild(), "label.feedback").subscribe(title -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(title)
                            .setColor(Color.GREEN)
                            .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                            .setDescription("```" + event.getValue("re_feedback_text").getAsString() + "```")
                            .setFooter("By " + event.getUser().getEffectiveName() + " (" + event.getUser().getId() + ")", event.getUser().getEffectiveAvatarUrl())
                            .setTimestamp(Instant.now());

                    Main.getInstance().getCommandManager().sendMessage(embedBuilder, BotWorker.getShardManager().getTextChannelById(BotConfig.getFeedbackChannel()));
                    Main.getInstance().getCommandManager().sendMessage("Thank you!", null, event.getInteraction().getHook());
                });
            }

            case "re_suggestion_modal" -> {
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Suggestions(), "FROM Suggestions WHERE guildChannelId.guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(suggestions -> {
                    event.deferReply(true).queue();

                    if (suggestions.isPresent()) {
                        MessageChannel messageChannel = (MessageChannel) event.getGuild().getGuildChannelById(suggestions.get().getGuildChannelId().getChannelId());

                        if (messageChannel == null) return;

                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.suggestion").block())
                                .setColor(Color.ORANGE)
                                .setThumbnail(event.getMember().getEffectiveAvatarUrl())
                                .setDescription("```" + event.getValue("re_suggestion_text").getAsString() + "```")
                                .setFooter(LanguageService.getByGuild(event.getGuild(), "message.suggestion.footer", event.getUser().getEffectiveName()).block(), event.getMember().getEffectiveAvatarUrl())
                                .setTimestamp(Instant.now());

                        Main.getInstance().getCommandManager().sendMessage(embedBuilder, messageChannel);
                        Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.suggestion.sent"), null, event.getInteraction().getHook());
                    } else {
                        Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.suggestion.notSetup"), null, event.getInteraction().getHook());
                    }
                });
            }

            case "re_music_add_modal" -> {
                event.deferReply(true).queue();
                Main.getInstance().getMusicWorker().playSong(event.getValue("re_music_add_modal_song").getAsString(),
                        event.getGuild(), event.getMember(), event.getGuildChannel(), event.getInteraction().getHook());
            }

            case "statisticsSetupTwitchModal" -> {
                ModalMapping modalMapping = event.getValue("twitchChannelName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                LanguageService.getByGuild(event.getGuild(), "label.statistics").subscribe(label -> {
                    String twitchUsername = modalMapping.getAsString();

                    java.util.List<Category> categories = event.getGuild().getCategoriesByName(label, true);

                    Category category;

                    if (categories.isEmpty()) {
                        category = event.getGuild().createCategory(label).complete();
                    } else {
                        category = categories.get(0);
                    }

                    String channelId = Main.getInstance().getNotifier().getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(twitchUsername)).execute().getUsers().get(0).getId();

                    SQLSession.getSqlConnector().getSqlWorker().getEntity(new TwitchIntegration(), "FROM TwitchIntegration WHERE channelId=:twitchId", Map.of("twitchId", channelId)).subscribe(twitchIntegration -> {
                        if (twitchIntegration.isPresent()) {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                    .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                    .setColor(Color.RED)
                                    .setDescription(LanguageService.getByGuild(event.getGuild(), "message.stream-action.noTwitch", BotConfig.getTwitchAuth()).block());
                            event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                            return;
                        }

                        event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.twitchCountName", Main.getInstance().getNotifier().getTwitchClient().getHelix().getChannelFollowers(null, channelId, null, 1, null).execute().getTotal()).block(), category)
                                .addPermissionOverride(event.getGuild().getPublicRole(), 0, Permission.VOICE_CONNECT.getRawValue()).queue(voiceChannel -> {
                                    SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(channelStats -> {
                                        if (channelStats.isPresent()) {
                                            ChannelStats channelStatsEntity = channelStats.get();
                                            if (channelStatsEntity.getTwitchFollowerChannelId() != null) {
                                                VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStatsEntity.getTwitchFollowerChannelId());

                                                if (voiceChannel3 != null)
                                                    voiceChannel3.delete().queue();
                                            }

                                            channelStatsEntity.setTwitchFollowerChannelId(voiceChannel.getId());
                                            channelStatsEntity.setTwitchFollowerChannelUsername(twitchUsername);
                                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStatsEntity).block();
                                            Main.getInstance().getNotifier().getTwitchSonic().add(twitchUsername);
                                        } else {
                                            ChannelStats channelStatsEntity = new ChannelStats(event.getGuild().getIdLong(),
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
                                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStatsEntity).block();
                                            Main.getInstance().getNotifier().getTwitterSonic().add(twitchUsername);
                                        }
                                    });
                                });

                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setColor(Color.GREEN)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.twitchSuccess").block());
                        event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                });
            }

            case "statisticsSetupYouTubeModal" -> {
                ModalMapping modalMapping = event.getValue("youtubeChannelName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                LanguageService.getByGuild(event.getGuild(), "label.statistics").subscribe(label -> {
                    String youtubeChannelName = modalMapping.getAsString();

                    java.util.List<Category> categories = event.getGuild().getCategoriesByName(label, true);

                    Category category;

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.youtubeSuccess").block());

                    if (categories.isEmpty()) {
                        category = event.getGuild().createCategory(label).complete();
                    } else {
                        category = categories.get(0);
                    }

                    ChannelResult youTubeChannel;
                    try {
                        if (YouTubeAPIHandler.getInstance().isValidChannelId(youtubeChannelName)) {
                            youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelById(youtubeChannelName);
                        } else {
                            youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelBySearch(youtubeChannelName);
                        }
                    } catch (Exception e) {
                        embedBuilder
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.retrievalError").block());
                        event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        return;
                    }

                    if (youTubeChannel == null) {
                        embedBuilder = embedBuilder
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.youtubeNotFound").block());
                        event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        return;
                    }

                    event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.youtubeCountName", youTubeChannel.getSubscriberCountText()).block(), category)
                            .addPermissionOverride(event.getGuild().getPublicRole(), 0, Permission.VOICE_CONNECT.getRawValue()).queue(voiceChannel -> {
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(channelStatsOptional -> {
                                    if (channelStatsOptional.isPresent()) {
                                        ChannelStats channelStats = channelStatsOptional.get();
                                        if (channelStats.getYoutubeSubscribersChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getYoutubeSubscribersChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }

                                        channelStats.setYoutubeSubscribersChannelId(voiceChannel.getId());
                                        channelStats.setYoutubeSubscribersChannelUsername(youTubeChannel.getId());
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStats).block();
                                        Main.getInstance().getNotifier().getYouTubeSonic().add(youTubeChannel.getId());
                                    } else {
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new ChannelStats(event.getGuild().getIdLong(),
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
                                                youTubeChannel.getId(),
                                                null,
                                                null)).block();
                                        Main.getInstance().getNotifier().getYouTubeSonic().add(youTubeChannel.getId());
                                    }
                                });
                            });

                    embedBuilder = embedBuilder
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.youtubeSuccess").block());
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                });
            }

            case "statisticsSetupRedditModal" -> {
                ModalMapping modalMapping = event.getValue("subredditName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                LanguageService.getByGuild(event.getGuild(), "label.statistics").subscribe(label -> {
                    String subredditName = modalMapping.getAsString();

                    java.util.List<Category> categories = event.getGuild().getCategoriesByName(label, true);

                    Category category;

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.redditSuccess").block());

                    if (categories.isEmpty()) {
                        category = event.getGuild().createCategory(label).complete();
                    } else {
                        category = categories.get(0);
                    }

                    RedditSubreddit subreddit;
                    try {
                        subreddit = Main.getInstance().getNotifier().getRedditSonic().getSubreddit(subredditName);
                    } catch (IOException | InterruptedException e) {
                        embedBuilder
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.retrievalError").block());
                        event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        return;
                    }

                    event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.redditCountName", subreddit.getActiveUserCount()).block(), category)
                            .addPermissionOverride(event.getGuild().getPublicRole(), 0, Permission.VOICE_CONNECT.getRawValue()).queue(voiceChannel -> {
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(channelStatsOptional -> {
                                    if (channelStatsOptional.isPresent()) {
                                        ChannelStats channelStats = channelStatsOptional.get();
                                        if (channelStats.getSubredditMemberChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getSubredditMemberChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }

                                        channelStats.setSubredditMemberChannelId(voiceChannel.getId());
                                        channelStats.setSubredditMemberChannelSubredditName(subredditName);
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStats).block();
                                        Main.getInstance().getNotifier().getRedditSonic().add(subredditName);
                                    } else {
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new ChannelStats(event.getGuild().getIdLong(),
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
                                                subredditName)).block();
                                        Main.getInstance().getNotifier().getRedditSonic().add(subredditName);
                                    }
                                });
                            });

                    embedBuilder = embedBuilder
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.redditSuccess").block());
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                });
            }

            case "statisticsSetupTwitterModal" -> {
                ModalMapping modalMapping = event.getValue("twitterName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                LanguageService.getByGuild(event.getGuild(), "label.statistics").subscribe(label -> {
                    String twitterName = modalMapping.getAsString();

                    java.util.List<Category> categories = event.getGuild().getCategoriesByName(label, true);

                    Category category;

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.twitterSuccess").block());

                    if (categories.isEmpty()) {
                        category = event.getGuild().createCategory(label).complete();
                    } else {
                        category = categories.get(0);
                    }

                    UserV2 twitterUser;
                    try {
                        twitterUser = Main.getInstance().getNotifier().getTwitterClient().getUserFromUserName(twitterName);
                    } catch (NoSuchElementException e) {
                        embedBuilder
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.retrievalError").block());
                        event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        return;
                    }

                    event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.twitterCountName", twitterUser.getFollowersCount()).block(), category)
                            .addPermissionOverride(event.getGuild().getPublicRole(), 0, Permission.VOICE_CONNECT.getRawValue()).queue(voiceChannel -> {
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(channelStatsOptional -> {
                                    if (channelStatsOptional.isPresent()) {
                                        ChannelStats channelStats = channelStatsOptional.get();
                                        if (channelStats.getTwitterFollowerChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getTwitterFollowerChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }

                                        channelStats.setTwitterFollowerChannelId(voiceChannel.getId());
                                        channelStats.setTwitterFollowerChannelUsername(twitterName);
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStats).block();
                                        Main.getInstance().getNotifier().getTwitterSonic().add(twitterName);
                                    } else {
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new ChannelStats(event.getGuild().getIdLong(),
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
                                                null)).block();
                                        Main.getInstance().getNotifier().getTwitterSonic().add(twitterName);
                                    }
                                });
                            });

                    embedBuilder = embedBuilder
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.twitterSuccess").block());
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                });
            }

            case "statisticsSetupInstagramModal" -> {
                ModalMapping modalMapping = event.getValue("instagramName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                LanguageService.getByGuild(event.getGuild(), "label.statistics").subscribe(label -> {
                    String instagramName = modalMapping.getAsString();

                    java.util.List<Category> categories = event.getGuild().getCategoriesByName(label, true);

                    Category category;

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.instagramSuccess").block());

                    if (categories.isEmpty()) {
                        category = event.getGuild().createCategory(label).complete();
                    } else {
                        category = categories.get(0);
                    }

                    com.github.instagram4j.instagram4j.models.user.User instagramUser;
                    try {
                        instagramUser = Main.getInstance().getNotifier().getInstagramClient().getActions().users().findByUsername(instagramName).get().getUser();
                    } catch (ExecutionException | InterruptedException e) {
                        embedBuilder
                                .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                                .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.retrievalError").block());
                        event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        return;
                    }

                    event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.instagramCountName", instagramUser.getFollower_count()).block(), category)
                            .addPermissionOverride(event.getGuild().getPublicRole(), 0, Permission.VOICE_CONNECT.getRawValue()).queue(voiceChannel -> {
                                SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(channelStatsOptional -> {
                                    if (channelStatsOptional.isPresent()) {
                                        ChannelStats channelStats = channelStatsOptional.get();
                                        if (channelStats.getInstagramFollowerChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getInstagramFollowerChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }

                                        channelStats.setInstagramFollowerChannelId(voiceChannel.getId());
                                        channelStats.setInstagramFollowerChannelUsername(instagramName);
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStats).block();
                                        Main.getInstance().getNotifier().getInstagramSonic().add(instagramName);
                                    } else {
                                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new ChannelStats(event.getGuild().getIdLong(),
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
                                                null)).block();
                                        Main.getInstance().getNotifier().getInstagramSonic().add(instagramName);
                                    }
                                });

                            });

                    embedBuilder = embedBuilder
                            .setTitle(LanguageService.getByGuild(event.getGuild(), "label.setupMenu").block())
                            .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(event.getGuild(), "message.statistics.instagramSuccess").block());
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                });
            }

            default -> event.deferEdit().setEmbeds(new EmbedBuilder()
                    .setTitle(LanguageService.getByGuild(event.getGuild(), "label.unknownMenu").block())
                    .setFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl())
                    .setColor(Color.RED)
                    .setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.unknownMenu").block())
                    .build()).setComponents(new ArrayList<>()).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        super.onStringSelectInteraction(event);

        if (event.getInteraction().getComponent().getId() == null ||
                event.getGuild() == null)
            return;

        if (event.getMessage().getEmbeds().isEmpty() ||
                event.getMessage().getEmbeds().get(0) == null)
            return;

        if (event.getInteraction().getValues().isEmpty() && !event.getInteraction().getComponent().getId().equals("setupAutoRole"))
            return;

        switch (event.getInteraction().getComponent().getId()) {
            case "setupAutoRole" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(event.getGuild().getIdLong()).subscribe(roles -> {
                    // We are doing this because a normal List can't be modified.
                    ArrayList<String> values = new ArrayList<>(event.getValues());

                    EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                    if (event.getSelectedOptions().isEmpty()) {
                        roles.forEach(autoRole ->
                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(autoRole).block());
                    } else {
                        roles.forEach(autoRole -> {
                            String value = String.valueOf(autoRole.getRoleId());

                            if (!event.getValues().contains(value)) {
                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(autoRole).block();
                                values.remove(value);
                            }
                        });

                        for (String roleId : values) {
                            Role role = event.getGuild().getRoleById(roleId);
                            if (role != null) {
                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(new AutoRole(event.getGuild().getIdLong(), role.getIdLong())).block();
                            }
                        }
                    }

                    embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.autoRole.setupSuccess").block());
                    event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                });
            }

            case "setupActionMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                java.util.List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "rewards" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.rewards").subscribe(rewardLabel -> {
                            TextInput blackJackWin = TextInput.create("re_rewards_BlackJackWin", LanguageService.getByGuild(event.getGuild(), "label.blackJackWin").block(), TextInputStyle.SHORT).setRequired(true).setMinLength(1).build();
                            TextInput musicQuizWin = TextInput.create("re_rewards_MusicQuizWin", LanguageService.getByGuild(event.getGuild(), "label.musicQuizWin").block(), TextInputStyle.SHORT).setRequired(true).setMinLength(1).build();
                            TextInput musicQuizFeature = TextInput.create("re_rewards_MusicQuizFeature", LanguageService.getByGuild(event.getGuild(), "label.musicQuizFeatureGuess").block(), TextInputStyle.SHORT).setRequired(true).setMinLength(1).build();
                            TextInput musicQuizArtist = TextInput.create("re_rewards_MusicQuizArtist", LanguageService.getByGuild(event.getGuild(), "label.musicQuizArtistGuess").block(), TextInputStyle.SHORT).setRequired(true).setMinLength(1).build();
                            TextInput musicQuizTitle = TextInput.create("re_rewards_MusicQuizTitle", LanguageService.getByGuild(event.getGuild(), "label.musicQuizTitleGuess").block(), TextInputStyle.SHORT).setRequired(true).setMinLength(1).build();
                            Modal modal = Modal.create("re_rewards_modal", rewardLabel).addActionRow(blackJackWin).addActionRow(musicQuizWin).addActionRow(musicQuizFeature).addActionRow(musicQuizArtist).addActionRow(musicQuizTitle).build();
                            event.replyModal(modal).queue();
                        });
                    }

                    case "lang" -> {
                        LanguageService.getByGuild(event.getGuild(), "message.setup.steps.lang").subscribe(description -> {
                            for (DiscordLocale locale : LanguageService.getSupported()) {
                                optionList.add(SelectOption.of(locale.getLanguageName(), locale.getLocale()));
                            }

                            embedBuilder.setDescription(description);

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupLangMenu", LanguageService.getByGuild(event.getGuild(), "message.default.actionRequired").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "log" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.setup").subscribe(label -> {
                            optionList.add(SelectOption.of(label, "logSetup"));

                            if (SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).block())
                                optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.delete").block(), "logDelete"));

                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.backToMenu").block(), "backToSetupMenu"));

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.setup.steps.auditLog").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupLogMenu", LanguageService.getByGuild(event.getGuild(), "message.default.actionRequired").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "welcome" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.setup").subscribe(label -> {
                            optionList.add(SelectOption.of(label, "welcomeSetup"));

                            SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getIdLong()).block().ifPresent(welcomeWebhook -> {
                                optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.delete").block(), "welcomeDelete"));
                            });

                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setImage").block(), "welcomeImage"));

                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.backToMenu").block(), "backToSetupMenu"));

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.setup.steps.welcome").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupWelcomeMenu", LanguageService.getByGuild(event.getGuild(), "message.default.actionRequired").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "autorole" ->
                            Setup.createAutoRoleSetupSelectMenu(event.getGuild(), event.getHook()).subscribe(selectMenu ->
                                    event.editMessageEmbeds(Setup.createAutoRoleSetupMessage(event.getGuild(), event.getHook()).build())
                                            .setComponents(
                                                    ActionRow.of(selectMenu),
                                                    ActionRow.of(Button.link(BotConfig.getWebinterface(), "Webinterface")))
                                            .queue());

                    case "tempvoice" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.setup").subscribe(label -> {
                            optionList.add(SelectOption.of(label, "tempVoiceSetup"));

                            SQLSession.getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid",
                                    Map.of("gid", event.getGuild().getIdLong())).block().ifPresent(temporalVoicechannel -> {
                                optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.delete").block(), "tempVoiceDelete"));
                            });


                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.backToMenu").block(), "backToSetupMenu"));

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.setup.steps.temporalVoice").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupTempVoiceMenu", LanguageService.getByGuild(event.getGuild(), "message.default.actionRequired").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "statistics" -> {
                        LanguageService.getByGuild(event.getGuild(), "message.statistics.setup").subscribe(description -> {
                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setupMemberStatistics").block(), "statisticsSetupMember"));
                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setupTwitchStatistics").block(), "statisticsSetupTwitch"));
                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setupYoutubeStatistics").block(), "statisticsSetupYouTube"));
                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setupRedditStatistics").block(), "statisticsSetupReddit"));
                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setupTwitterStatistics").block(), "statisticsSetupTwitter"));
                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.setupInstagramStatistics").block(), "statisticsSetupInstagram"));

                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.backToMenu").block(), "backToSetupMenu"));

                            embedBuilder.setDescription(description);

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupStatisticsMenu", LanguageService.getByGuild(event.getGuild(), "message.default.actionRequired").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "tickets" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.setup").subscribe(label -> {
                            optionList.add(SelectOption.of(label, "ticketsSetup"));

                            SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).block().ifPresent(tickets -> {
                                optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.delete").block(), "ticketsDelete"));
                            });

                            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.backToMenu").block(), "backToSetupMenu"));

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.ticket.setup").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupTicketsMenu", LanguageService.getByGuild(event.getGuild(), "message.default.actionRequired").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    default -> {
                        LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).queue();
                        });
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
                        LanguageService.getByGuild(event.getGuild(), "message.statistics.memberSuccess").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            embedBuilder.setColor(Color.GREEN);
                            event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                            java.util.List<Category> categories = event.getGuild().getCategoriesByName(LanguageService.getByGuild(event.getGuild(), "label.statistics").block(), true);

                            Category category;

                            if (categories.isEmpty()) {
                                category = event.getGuild().createCategory(LanguageService.getByGuild(event.getGuild(), "label.statistics").block()).complete();
                            } else {
                                category = categories.get(0);
                            }

                            event.getGuild().loadMembers().onSuccess(members -> event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.overallMembersName", event.getGuild().getMemberCount()).block(), category).queue(voiceChannel -> {
                                voiceChannel.getManager().setUserLimit(0).queue();
                                event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.realMembersName", members.stream().filter(member -> !member.getUser().isBot()).count()).block(), category).queue(voiceChannel1 -> {
                                    voiceChannel1.getManager().setUserLimit(0).queue();
                                    event.getGuild().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.botMembersName", members.stream().filter(member -> member.getUser().isBot()).count()).block(), category).queue(voiceChannel2 -> {
                                        voiceChannel2.getManager().setUserLimit(0).queue();
                                        SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(channelStatsOptional -> {
                                            if (channelStatsOptional.isPresent()) {
                                                ChannelStats channelStats = channelStatsOptional.get();
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
                                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(channelStats).block();
                                            } else {
                                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(new ChannelStats(event.getGuild().getIdLong(),
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
                                                        null)).block();
                                            }
                                        });

                                    });
                                });
                            }));
                        });
                    }

                    case "statisticsSetupTwitch" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.channelName").subscribe(label -> {
                            TextInput input = TextInput.create("twitchChannelName", label, TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Twitch Channel name here!").build();

                            Modal modal = Modal.create("statisticsSetupTwitchModal", LanguageService.getByGuild(event.getGuild(), "label.setupTwitchStatistics").block()).addActionRow(input).build();

                            event.replyModal(modal).queue();
                        });

                    }

                    case "statisticsSetupYouTube" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.channelName").subscribe(label -> {
                            TextInput input = TextInput.create("youtubeChannelName", label, TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the YouTube Channel name here!").build();

                            Modal modal = Modal.create("statisticsSetupYouTubeModal", LanguageService.getByGuild(event.getGuild(), "label.setupYoutubeStatistics").block()).addActionRow(input).build();

                            event.replyModal(modal).queue();
                        });
                    }

                    case "statisticsSetupReddit" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.subreddit").subscribe(label -> {
                            TextInput input = TextInput.create("subredditName", label, TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Subreddit name here!").build();

                            Modal modal = Modal.create("statisticsSetupRedditModal", LanguageService.getByGuild(event.getGuild(), "label.setupRedditStatistics").block()).addActionRow(input).build();

                            event.replyModal(modal).queue();
                        });
                    }

                    case "statisticsSetupTwitter" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.name").subscribe(label -> {
                            TextInput input = TextInput.create("twitterName", label, TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Twitter name here!").build();

                            Modal modal = Modal.create("statisticsSetupTwitterModal", LanguageService.getByGuild(event.getGuild(), "label.setupTwitterStatistics").block()).addActionRow(input).build();

                            event.replyModal(modal).queue();
                        });

                    }

                    case "statisticsSetupInstagram" -> {
                        LanguageService.getByGuild(event.getGuild(), "label.name").subscribe(label -> {
                            TextInput input = TextInput.create("instagramName", label, TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Instagram name here!").build();

                            Modal modal = Modal.create("statisticsSetupInstagramModal", LanguageService.getByGuild(event.getGuild(), "label.setupInstagramStatistics").block()).addActionRow(input).build();

                            event.replyModal(modal).queue();
                        });

                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).queue();
                        });
                    }
                }
            }

            case "setupTicketsMenu" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "ticketsSetup" -> {
                        LanguageService.getByGuild(event.getGuild(), "message.ticket.setupDescription").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).setComponents().queue();
                        });
                    }

                    case "ticketsDelete" -> {
                        SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(tickets -> {
                            if (tickets.isPresent()) {
                                embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.ticket.deleted").block());
                                embedBuilder.setColor(Color.GREEN);
                                event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(tickets.get()).block();
                            }
                        });
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).queue();
                        });
                    }
                }
            }

            case "setupTempVoicechannel" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                String value = event.getInteraction().getValues().get(0);

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                if (value.equalsIgnoreCase("more")) {
                    LanguageService.getByGuild(event.getGuild(), "message.temporalVoice.setupDescription").subscribe(description -> {
                        java.util.List<SelectOption> optionList = new ArrayList<>();

                        for (VoiceChannel channel : event.getGuild().getVoiceChannels().stream().skip(24).toList()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription(description);

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupTempVoicechannel", LanguageService.getByGuild(event.getGuild(), "label.selectChannel").block(), 1, 1, false, optionList)).queue();
                    });
                    return;
                }

                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(value);

                if (voiceChannel != null) {
                    LanguageService.getByGuild(event.getGuild(), "message.temporalVoice.setupSuccess").subscribe(description -> {
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new TemporalVoicechannel(event.getGuild().getIdLong(), voiceChannel.getIdLong())).block();
                        embedBuilder.setDescription(description);
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    LanguageService.getByGuild(event.getGuild(), "message.default.invalidOptionChannel").subscribe(description -> {
                        embedBuilder.setDescription(description);
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    });
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
                        LanguageService.getByGuild(event.getGuild(), "label.more").subscribe(labelMore -> {
                            for (VoiceChannel channel : event.getGuild().getVoiceChannels()) {
                                if (optionList.size() == 24) {
                                    optionList.add(SelectOption.of(labelMore, "more"));
                                    break;
                                }

                                optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                            }

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.temporalVoice.setupDescription").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupTempVoicechannel", LanguageService.getByGuild(event.getGuild(), "label.selectChannel").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "tempVoiceDelete" -> {
                        SQLSession.getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(temporalVoicechannel -> {
                            if (temporalVoicechannel.isPresent()) {
                                embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.temporalVoice.deleted").block());
                                embedBuilder.setColor(Color.GREEN);
                                event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel.get()).block();
                            }
                        });
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).queue();
                        });
                    }
                }
            }

            case "setupLangMenu" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                DiscordLocale selectedLocale = DiscordLocale.from(event.getInteraction().getValues().get(0));

                if (selectedLocale != DiscordLocale.UNKNOWN && LanguageService.getSupported().contains(selectedLocale)) {
                    Language language = LanguageService.languageResources.get(selectedLocale);
                    LanguageService.getByGuild(event.getGuild(), "message.lang.setupSuccess", language.getName() + " by " + language.getAuthor()).subscribe(description -> {
                        SQLSession.getSqlConnector().getSqlWorker().setSetting(event.getGuild().getIdLong(), "configuration_language", "Language", selectedLocale.getLocale());
                        embedBuilder.setDescription(description);
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                        embedBuilder.setDescription(description);
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    });
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
                        LanguageService.getByGuild(event.getGuild(), "label.more").subscribe(labelMore -> {
                            for (TextChannel channel : event.getGuild().getTextChannels()) {
                                if (optionList.size() == 24) {
                                    optionList.add(SelectOption.of(labelMore, "more"));
                                    break;
                                }

                                optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                            }

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.auditLog.setupDescription").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupLogChannel", LanguageService.getByGuild(event.getGuild(), "label.selectChannel").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "logDelete" -> {
                        SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).subscribe(webhook ->
                                webhook.ifPresent(webhookLog -> event.getJDA().retrieveWebhookById(webhookLog.getChannelId()).queue(webhook1 -> {
                                    webhook1.delete().queue();
                                    embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.auditLog.deleted").block());
                                    embedBuilder.setColor(Color.GREEN);
                                    event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookLog).block();
                                })));
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).queue();
                        });
                    }
                }
            }

            case "setupLogChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                String value = event.getInteraction().getValues().get(0);

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                if (value.equals("more")) {
                    LanguageService.getByGuild(event.getGuild(), "label.more").subscribe(labelMore -> {
                        java.util.List<SelectOption> optionList = new ArrayList<>();

                        for (TextChannel channel : event.getGuild().getTextChannels().stream().skip(24).toList()) {
                            if (optionList.size() == 24) {
                                optionList.add(SelectOption.of(labelMore, "more"));
                                break;
                            }

                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.auditLog.setupDescription").block());

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupLogChannel", LanguageService.getByGuild(event.getGuild(), "label.selectChannel").block(), 1, 1, false, optionList)).queue();
                    });
                    return;
                }


                TextChannel textChannel = event.getGuild().getTextChannelById(value);

                if (textChannel != null) {
                    textChannel.createWebhook(BotConfig.getBotName() + "-Logs").queue(webhook -> {
                        SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).block().ifPresent(webhookEntity ->
                                WebhookUtil.deleteWebhook(event.getGuild().getIdLong(), webhookEntity));
                        SQLSession.getSqlConnector().getSqlWorker().setLogWebhook(event.getGuild().getIdLong(), textChannel.getIdLong(), webhook.getIdLong(), webhook.getToken());
                        embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.auditLog.setupSuccess").block());
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.default.invalidOptionChannel").block());
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
                        LanguageService.getByGuild(event.getGuild(), "label.more").subscribe(moreLabel -> {
                            for (TextChannel channel : event.getGuild().getTextChannels()) {
                                if (optionList.size() == 24) {
                                    optionList.add(SelectOption.of(moreLabel, "more"));
                                    break;
                                }

                                optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                            }

                            embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.welcome.setupDescription").block());

                            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupWelcomeChannel", LanguageService.getByGuild(event.getGuild(), "label.selectChannel").block(), 1, 1, false, optionList)).queue();
                        });
                    }

                    case "welcomeImage" -> {
                        LanguageService.getByGuild(event.getGuild(), "message.welcome.imageRequired").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).setComponents().queue();
                        });
                    }

                    case "welcomeDelete" -> {
                        SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getIdLong()).subscribe(webhook ->
                                webhook.ifPresent(webhookWelcome -> event.getJDA().retrieveWebhookById(webhookWelcome.getChannelId()).queue(webhook1 -> {
                                    webhook1.delete().queue();
                                    embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.welcome.deleted").block());
                                    embedBuilder.setColor(Color.GREEN);
                                    event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhookWelcome).block();
                                })));
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                            embedBuilder.setDescription(description);
                            event.editMessageEmbeds(embedBuilder.build()).queue();
                        });
                    }
                }
            }

            case "setupWelcomeChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                String value = event.getInteraction().getValues().get(0);

                if (value.equals("more")) {
                    LanguageService.getByGuild(event.getGuild(), "label.more").subscribe(labelMore -> {
                        java.util.List<SelectOption> optionList = new ArrayList<>();

                        for (TextChannel channel : event.getGuild().getTextChannels().stream().skip(24).toList()) {
                            if (optionList.size() == 24) {
                                optionList.add(SelectOption.of(labelMore, "more"));
                                break;
                            }

                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.welcome.setupDescription").block());

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupWelcomeChannel", LanguageService.getByGuild(event.getGuild(), "label.selectChannel").block(), 1, 1, false, optionList)).queue();
                    });
                    return;
                }

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (textChannel != null) {
                    textChannel.createWebhook(BotConfig.getBotName() + "-Welcome").queue(webhook -> {
                        // .block() can be called here because its being queued.
                        SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getIdLong()).block().ifPresent(webhookEntity ->
                                WebhookUtil.deleteWebhook(event.getGuild().getIdLong(), webhookEntity));

                        SQLSession.getSqlConnector().getSqlWorker().setWelcomeWebhook(event.getGuild().getIdLong(), textChannel.getIdLong(), webhook.getIdLong(), webhook.getToken());
                        embedBuilder.setDescription(LanguageService.getByGuild(event.getGuild(), "message.welcome.setupSuccess").block());
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    LanguageService.getByGuild(event.getGuild(), "message.default.invalidOptionChannel").subscribe(description -> {
                        embedBuilder.setDescription(description);
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    });
                }

            }

            default -> {
                if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null) return;

                LanguageService.getByGuild(event.getGuild(), "message.default.invalidOption").subscribe(description -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                    embedBuilder.setDescription(description);
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                });

            }
        }
    }

    /**
     * Called when the default choices should be sent.
     *
     * @param event The InteractionEvent of the SelectMenu.
     */
    public void sendDefaultChoice(StringSelectInteractionEvent event) {
        LanguageService.getByGuild(event.getGuild(), "message.setup.setupMenu").subscribe(description -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

            List<SelectOption> optionList = new ArrayList<>();
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.language").block(), "lang"));
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.auditLog").block(), "log"));
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.welcomeChannel").block(), "welcome"));
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.autoRole").block(), "autorole"));
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.temporalVoice").block(), "tempvoice"));
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.statistics").block(), "statistics"));
            optionList.add(SelectOption.of(LanguageService.getByGuild(event.getGuild(), "label.ticketSystem").block(), "tickets"));

            embedBuilder.setDescription(description);

            event.editMessageEmbeds(embedBuilder.build()).setActionRow(new StringSelectMenuImpl("setupActionMenu", LanguageService.getByGuild(event.getGuild(), "message.setup.setupMenuPlaceholder").block(), 1, 1, false, optionList)).queue();
        });
    }

    /**
     * Checks if the user has the required Permissions to use the Command.
     *
     * @param member  The Member who should be checked.
     * @param channel The Channel used.
     * @return True if the user does not have the required Permissions, false if otherwise.
     */
    private boolean checkPerms(Member member, MessageChannel channel) {
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage(LanguageService.getByGuild((member == null ? null : member.getGuild()), "message.default.insufficientPermission", Permission.ADMINISTRATOR.name()).block()).queue();
            return true;
        }

        if (!member.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            channel.sendMessage(LanguageService.getByGuild(member.getGuild(), "message.default.needPermission", Permission.MANAGE_WEBHOOKS.name()).block()).queue();
            return true;
        }

        return false;
    }
}
