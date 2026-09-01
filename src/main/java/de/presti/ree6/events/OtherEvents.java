package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.audio.AudioPlayerReceiveHandler;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.ReactionRole;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.entities.Tickets;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.apis.ChatGPTAPI;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.ImageCreationUtility;
import de.presti.ree6.utils.data.TranscriptUtil;
import de.presti.ree6.utils.others.*;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Event Handler for no categorized Events.
 */
@Slf4j
public class OtherEvents extends ListenerAdapter {

    /**
     * How long a Member may not earn chat XP again after having earned some.
     */
    private static final Duration XP_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Upper bound on how many messages a ticket transcript will contain.
     */
    private static final int TRANSCRIPT_MESSAGE_LIMIT = 1_000;

    /**
     * @inheritDoc
     */
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotWorker.setState(BotState.STARTED);
        log.info("Boot up finished!");

        Main.getInstance().getCommandManager().addSlashCommand(event.getJDA());

        BotWorker.setActivity(event.getJDA(), BotConfig.getStatus(), Activity.ActivityType.CUSTOM_STATUS);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        Mono.fromRunnable(() -> SQLSession.getSqlConnector().getSqlWorker().createCommandSettings(guildId))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(null, throwable -> log.error("Failed to create the command settings for {}!", guildId, throwable));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        Mono.fromRunnable(() -> SQLSession.getSqlConnector().getSqlWorker().deleteAllData(guildId))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(null, throwable -> log.error("Failed to delete the data of {}!", guildId, throwable));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getId())).subscribe(channelStats -> {
            if (channelStats.isPresent()) {
                ChannelStats channelStatsEntity = channelStats.get();
                if (channelStatsEntity.getMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStatsEntity.getMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.overallMembers").block() + ": " + event.getGuild().getMemberCount()).queue();
                    }
                }

                event.getGuild().loadMembers().onSuccess(members -> {
                    if (channelStatsEntity.getRealMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStatsEntity.getRealMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.realMembers").block() + ": " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                        }
                    }

                    if (channelStatsEntity.getBotMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStatsEntity.getBotMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.botMembers").block() + ": " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                        }
                    }
                });
            }
        });

        GuildUtil.handleMemberJoin(event.getGuild(), event.getMember());

        SQLSession.getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getIdLong()).subscribe(x -> {
            if (x) {
                WebhookMessageBuilder wmb = new WebhookMessageBuilder();

                wmb.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                wmb.setUsername("Welcome!");

                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "message_join").subscribe(messageSetting -> {
                    if (messageSetting.isPresent()) {
                        final String messageContent = messageSetting.get().getStringValue()
                                .replace("%user_name%", event.getMember().getUser().getName())
                                .replace("%guild_name%", event.getGuild().getName())
                                .replace("%guild_member_count%", String.valueOf(event.getGuild().getMemberCount()));

                        SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "message_join_image").subscribe(joinImage -> {
                            if (joinImage.isPresent() && !joinImage.get().getStringValue().isBlank()) {
                                try {
                                    wmb.addFile("welcome.png", ImageCreationUtility.createJoinImage(event.getUser(), joinImage.get().getStringValue(),
                                            messageContent.replace("%user_mention%", event.getMember().getUser().getName())));
                                } catch (IOException e) {
                                    wmb.setContent(messageContent.replace("%user_mention%", event.getMember().getUser().getAsMention()));
                                    log.error("Error while creating join image!", e);
                                }
                            } else {
                                wmb.setContent(messageContent.replace("%user_mention%", event.getMember().getUser().getAsMention()));
                            }

                            SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getIdLong()).subscribe(webhook -> {
                                if (webhook.isEmpty()) return;

                                WebhookUtil.sendWebhook(wmb.build(), webhook.get(), WebhookUtil.WebhookTyp.WELCOME);
                            });
                        });
                    }
                });
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        super.onGuildMemberRemove(event);

        SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getId())).subscribe(channelStats -> {
            if (channelStats.isPresent()) {
                ChannelStats channelStatsEntity = channelStats.get();
                if (channelStatsEntity.getMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStatsEntity.getMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.overallMembers").block() + ": " + event.getGuild().getMemberCount()).queue();
                    }
                }

                event.getGuild().loadMembers().onSuccess(members -> {
                    if (channelStatsEntity.getRealMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStatsEntity.getRealMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.realMembers").block() + ": " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                        }
                    }

                    if (channelStatsEntity.getBotMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStatsEntity.getBotMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.botMembers").block() + ": " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                        }
                    }
                });
            }
        });

        if (BotConfig.isModuleActive("tickets")) {
            SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).subscribe(tickets -> {
                if (tickets.isPresent()) {
                    Tickets ticketsEntity = tickets.get();
                    Category category = event.getGuild().getCategoryById(ticketsEntity.getTicketCategory());

                    if (category != null) {
                        List<TextChannel> channels = category.getTextChannels().stream().filter(c -> c.getTopic() != null && c.getTopic().equalsIgnoreCase(event.getUser().getId())).toList();
                        if (!channels.isEmpty()) {
                            TextChannel channel = channels.get(0);

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            webhookMessageBuilder.setUsername(BotConfig.getBotName() + "-Tickets");

                            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                            webhookEmbedBuilder.setDescription("Here is the transcript of the ticket " + ticketsEntity.getTicketCount() + "!");
                            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                            webhookEmbedBuilder.setColor(BotConfig.getMainColor().getRGB());

                            webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                            channel.getIterableHistory().takeAsync(TRANSCRIPT_MESSAGE_LIMIT).thenAccept(history -> {
                                List<Message> messages = new ArrayList<>(history);
                                Collections.reverse(messages);

                                webhookMessageBuilder.addFile(event.getGuild().getId() + "_" + ticketsEntity.getTicketCount() + "_transcript.html",
                                        TranscriptUtil.generateTranscript(event.getJDA(), messages,
                                                channel.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)).replace("T", " "),
                                                ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)).replace("T", " ")).getBytes(StandardCharsets.UTF_8));

                                WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), ticketsEntity.getLogChannelId(), ticketsEntity.getLogChannelWebhookToken(), WebhookUtil.WebhookTyp.TICKET);
                                channel.delete().queue();
                            }).exceptionally(throwable -> {
                                log.error("Failed to build the ticket transcript for {}!", channel.getId(), throwable);
                                return null;
                            });
                        }
                    }
                }
            });
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
            if (event.getEntity().getIdLong() == event.getGuild().getSelfMember().getIdLong()) {
                if (event.getGuild().getAudioManager().getReceivingHandler() != null) {
                    event.getEntity().deafen(false).queue();
                }
            }

            if (BotConfig.isModuleActive("temporalvoice")) {
                checkCreationChannel(event.getGuild(), event.getMember(), event.getChannelJoined().getIdLong());
            }

            if (!ArrayUtil.voiceJoined.containsKey(event.getMember()) && !event.getEntity().getUser().isBot()) {
                GuildVoiceState voiceState = event.getVoiceState();

                if (voiceState.isMuted() && BotConfig.shouldResetOnMute()) return;
                if (voiceState.isGuildMuted() && BotConfig.shouldResetOnMuteGlobal()) return;
                if (voiceState.isDeafened() && BotConfig.shouldResetOnDeafen()) return;
                if (voiceState.isGuildDeafened() && BotConfig.shouldResetOnDeafenGlobal()) return;

                ArrayUtil.voiceJoined.put(event.getMember(), System.currentTimeMillis());
            }
        } else if (event.getChannelJoined() == null) {
            doVoiceXPStuff(event.getMember());

            if (event.getChannelLeft().getMembers().size() == 1 &&
                    event.getChannelLeft().getMembers().get(0).getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
                AudioManager audioManager = event.getGuild().getAudioManager();

                AudioPlayerReceiveHandler handler = (AudioPlayerReceiveHandler) audioManager.getReceivingHandler();

                if (handler != null) {
                    handler.endReceiving();
                }

                Main.getInstance().getMusicWorker().getGuildAudioPlayer(event.getGuild()).getScheduler().stopAll(null);
                Main.getInstance().getMusicWorker().disconnect(event.getGuild());
            }

            if (BotConfig.isModuleActive("temporalvoice")) {
                checkChannel(event.getChannelLeft(), event.getJDA());
            }
        } else {
            if (BotConfig.isModuleActive("temporalvoice")) {
                if (checkChannel(event.getChannelLeft(), event.getJDA())) return;

                checkCreationChannel(event.getGuild(), event.getMember(), event.getChannelJoined().getIdLong());
            }
        }
    }

    private void checkCreationChannel(Guild guild, Member member, long channelId) {
        SQLSession.getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", guild.getId()))
                .subscribe(temporalVoicechannel -> {
                    if (temporalVoicechannel.isPresent()) {
                        TemporalVoicechannel temporalVoicechannelEntity = temporalVoicechannel.get();
                        VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);

                        if (voiceChannel == null)
                            return;

                        if (temporalVoicechannelEntity.getGuildChannelId().getChannelId() != voiceChannel.getIdLong()) {
                            return;
                        }

                        if (voiceChannel.getParentCategory() != null) {
                            LanguageService.getByGuild(guild, "label.temporalVoiceName", "SPLIT").subscribe(preName -> {
                                preName = preName.split("SPLIT")[0];
                                String finalPreName = preName;
                                LanguageService.getByGuild(guild, "label.temporalVoiceName",
                                                guild.getVoiceChannels().stream().filter(c -> c.getName().startsWith(finalPreName)).count() + 1)
                                        .subscribe(name -> voiceChannel.getParentCategory().createVoiceChannel(name)
                                                .queue(channel -> {
                                                    guild.moveVoiceMember(member, channel).queue();
                                                    ArrayUtil.temporalVoicechannel.add(channel.getId());
                                                }));
                            });
                        }
                    }
                });
    }

    private boolean checkChannel(AudioChannelUnion channel, JDA instance) {
        if (ArrayUtil.isTemporalVoicechannel(channel)
                && (channel.getMembers().isEmpty() || (channel.getMembers().size() == 1 &&
                channel.getMembers().get(0).getIdLong() == instance.getSelfUser().getIdLong()))) {
            channel.delete().queue(c -> ArrayUtil.temporalVoicechannel.remove(channel.getId()));
            return true;
        }

        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {

        if (!(event instanceof GuildVoiceDeafenEvent || event instanceof GuildVoiceGuildDeafenEvent || event instanceof GuildVoiceGuildMuteEvent || event instanceof GuildVoiceMuteEvent)) {
            return;
        }

        if (event instanceof GuildVoiceGuildDeafenEvent guildDeafenEvent) {
            if (event.getMember() == event.getGuild().getSelfMember() &&
                    !guildDeafenEvent.isGuildDeafened()) {
                if (event.getGuild().getAudioManager().getReceivingHandler() == null) {
                    event.getGuild().getSelfMember().deafen(true).queue();
                }
            }
        }

        if (event.getMember().getUser().isBot()) return;

        GuildVoiceState voiceState = event.getVoiceState();

        boolean toggleOn = (BotConfig.shouldResetOnDeafenGlobal() && voiceState.isGuildDeafened()) ||
                (BotConfig.shouldResetOnDeafen() && voiceState.isDeafened()) ||
                (BotConfig.shouldResetOnMuteGlobal() && voiceState.isGuildMuted()) ||
                (BotConfig.shouldResetOnMute() && voiceState.isMuted());

        if (toggleOn) {
            doVoiceXPStuff(event.getMember());
        } else {
            if (!event.getVoiceState().inAudioChannel()) return;
            if (ArrayUtil.voiceJoined.containsKey(event.getMember())) return;

            ArrayUtil.voiceJoined.put(event.getMember(), System.currentTimeMillis());
        }
    }

    /**
     * Method used to do all the calculations for the Voice XP.
     *
     * @param member the Member that should be checked.
     */
    public void doVoiceXPStuff(Member member) {
        if (ArrayUtil.voiceJoined.containsKey(member)) {
            int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(member)));

            int addXP = IntStream.rangeClosed(1, min).map(i -> RandomUtils.random.nextInt(5, 11)).sum();

            SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(member.getGuild().getIdLong(), member.getIdLong()).subscribe(x -> {
                x.addExperience(addXP);

                SQLSession.getSqlConnector().getSqlWorker().addVoiceLevelData(member.getGuild().getIdLong(), x);

                GuildUtil.handleVoiceLevelReward(member.getGuild(), member);
            });

            ArrayUtil.voiceJoined.remove(member);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        super.onMessageDelete(event);

        if (ArrayUtil.musicPanelList.containsKey(event.getGuild().getIdLong())) {
            long id = ArrayUtil.musicPanelList.get(event.getGuild().getIdLong()).getIdLong();
            if (id == event.getMessageIdLong()) {
                ArrayUtil.musicPanelList.remove(event.getGuild().getIdLong());
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.isFromType(ChannelType.NEWS) &&
                BotConfig.isModuleActive("autopublish")) {
            SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "configuration_autopublish").subscribe(x -> {
                if (x.isPresent() && x.get().getBooleanValue())
                    event.getMessage().crosspost().queue(c -> c.addReaction(Emoji.fromUnicode("U+1F4E2")).queue());
            });
        }

        if (event.isFromGuild() && (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.VOICE)) && event.getMember() != null) {

            if (event.getAuthor().isBot()) return;

            if (!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage());
            }

            if (!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
            }

            ModerationUtil.shouldModerate(event.getGuild().getIdLong()).publishOn(Schedulers.boundedElastic()).map(x -> {

                if (BotConfig.isDebug())
                    log.info("Message received from {} in {} with content: {}", event.getAuthor().getGlobalName(), event.getChannel().getName(), event.getMessage().getContentRaw());

                boolean moderated = false;
                if (x) {
                    moderated = Boolean.TRUE.equals(ModerationUtil.checkMessage(event.getGuild().getIdLong(), event.getMessage().getContentRaw()).block());

                    if (moderated) {
                        Main.getInstance().getCommandManager().deleteMessageWithoutException(event.getMessage(), null);

                        Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.blacklisted"), BotConfig.getModerationMessageDeleteDelay(), event.getChannel(), null);
                    }
                }

                if (BotConfig.isDebug())
                    log.info("Message was moderated: {}", moderated);

                return moderated;
            }).flatMap(aBoolean -> handleCommand(aBoolean, event)).doOnNext(handled -> {
                if (!handled) {
                    if (!event.getMessage().getMentions().getUsers().isEmpty() && event.getMessage().getMentions().getUsers().contains(event.getJDA().getSelfUser())) {
                        if (event.getMessage().getMessageReference() != null) return;

                        try {
                            String response = ChatGPTAPI.getResponse(event.getMember(), event.getMessage().getContentDisplay());

                            if (response != null && !response.isBlank())
                                Main.getInstance().getCommandManager().sendMessage(response, event.getChannel());
                        } catch (Exception e) {
                            Sentry.captureException(e);
                            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.default.retrievalError"), event.getChannel());
                        }
                    }

                    if (BotConfig.isModuleActive("level")) {
                       if (ArrayUtil.applyTimeout(event.getMember(), XP_TIMEOUT.toMillis())) {

                            SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(event.getGuild().getIdLong(), event.getMember().getIdLong()).subscribe(userLevel -> {
                                if (userLevel.addExperience(RandomUtils.random.nextInt(15, 26))) {
                                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "level_message").subscribe(z -> {
                                        if (z.isPresent() && z.get().getBooleanValue()) {
                                            LanguageService.getByGuild(event.getGuild(), "label.chat").subscribe(chatLabel ->
                                                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(),
                                                            "message.levelUp", userLevel.getLevel(), chatLabel,
                                                            event.getMember().getAsMention()), event.getChannel()));
                                        }
                                    });
                                }

                                SQLSession.getSqlConnector().getSqlWorker().addChatLevelData(event.getGuild().getIdLong(), userLevel);
                            });
                        }

                        GuildUtil.handleChatLevelReward(event.getGuild(), event.getMember());
                    }
                } else {
                    Main.getInstance().getCommandManager().timeoutUser(event.getAuthor());
                }
            }).subscribe();
        }
    }

    private Mono<Boolean> handleCommand(boolean moderated, MessageReceivedEvent event) {
        if (moderated) return Mono.just(true);
        return Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), event.getMessage().getContentRaw(), event.getMessage(), event.getGuildChannel(), null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!BotConfig.isModuleActive("reactionRoles")) return;

        if (event.getMember() == null) return;

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) return;

        if (!event.getGuild().getSelfMember().hasAccess(event.getGuildChannel())) return;

        if (!event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(), Permission.MESSAGE_HISTORY)) return;

        event.retrieveMessage().queue(message -> {

            EmojiUnion emojiUnion = event.getReaction().getEmoji();

            String reactionCode = emojiUnion.getAsReactionCode();

            long emojiId;
            if (event.getReaction().getEmoji().getType() == Emoji.Type.CUSTOM) {
                emojiId = Long.parseLong(reactionCode.split(":")[1]);
            } else {
                emojiId = reactionCode.replace(":", "").hashCode();
            }

            if (message.getAuthor().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId()) && message.getEmbeds().isEmpty()) {
                String messageContent = message.getContentRaw();

                LanguageService.getByGuild(event.getGuild(), "message.reactions.reactionNeeded", "SPLIT_HERE").subscribe(translation -> {
                    if (messageContent.startsWith(translation.split("SPLIT_HERE")[0])) {
                        if (event.getMember().hasPermission(Permission.ADMINISTRATOR) && message.getMessageReference() != null) {
                            if (message.getMentions().getRoles().isEmpty()) {
                                LanguageService.getByGuild(event.getGuild(), "message.reactions.roleNotFound").map(message::editMessage).subscribe(MessageEditAction::queue);
                                return;
                            }

                            Role role = message.getMentions().getRoles().get(0);

                            if (role == null) {
                                LanguageService.getByGuild(event.getGuild(), "message.reactions.roleNotFound").map(message::editMessage).subscribe(MessageEditAction::queue);
                                return;
                            }

                            ReactionRole reactionRole = new ReactionRole(event.getGuild().getIdLong(), emojiId, emojiUnion.getFormatted(), role.getIdLong(), message.getMessageReference().getMessageIdLong());
                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole).block();

                            if (message.getMessageReference().getMessage() != null) {
                                if (event.getEmoji().getType() == Emoji.Type.CUSTOM && !event.getEmoji().asRich().canInteract(event.getMember())) {
                                    // Might not be the smartest move, either way I do not care enough to find a better solution to fixing this.
                                    return;
                                }
                                message.getMessageReference().getMessage().addReaction(event.getEmoji()).queue();
                            }

                            LanguageService.getByGuild(event.getGuild(), "message.reactions.roleAssign", role.getAsMention())
                                    .subscribe(x -> message.editMessage(x).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue());
                        }
                    }
                });
            } else {
                SQLSession.getSqlConnector().getSqlWorker()
                        .getEntity(new ReactionRole(), "FROM ReactionRole WHERE guildRoleId.guildId=:gid AND emoteId=:emoteId AND messageId=:messageId",
                                Map.of("gid", event.getGuild().getIdLong(), "emoteId", emojiId, "messageId", message.getIdLong())).subscribe(reactionRole -> {
                            if (reactionRole.isPresent()) {
                                ReactionRole reactionRoleEntity = reactionRole.get();
                                Role role = event.getGuild().getRoleById(reactionRoleEntity.getId());

                                if (role != null) {
                                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                                }

                                boolean changes = false;

                                if (reactionRoleEntity.getChannelId() == 0) {
                                    reactionRoleEntity.setChannelId(event.getChannel().getIdLong());
                                    changes = true;
                                }

                                if (reactionRoleEntity.getFormattedEmote().isBlank()) {
                                    reactionRoleEntity.setFormattedEmote(emojiUnion.getFormatted());
                                    changes = true;
                                }

                                if (changes)
                                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRoleEntity).block();
                            }
                        });
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!BotConfig.isModuleActive("reactionRoles")) return;
        if (event.getMember() == null) return;

        String reactionCode = event.getReaction().getEmoji().getAsReactionCode();

        long emojiId;
        if (event.getReaction().getEmoji().getType() == Emoji.Type.CUSTOM) {
            emojiId = Long.parseLong(reactionCode.split(":")[1]);
        } else {
            emojiId = reactionCode.replace(":", "").hashCode();
        }

        SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(),
                "FROM ReactionRole WHERE guildRoleId.guildId=:gid AND emoteId=:emoteId AND messageId=:messageId",
                Map.of("gid", event.getGuild().getIdLong(), "emoteId", emojiId, "messageId", event.getMessageIdLong())).subscribe(reactionRole -> {
            if (reactionRole.isPresent()) {
                ReactionRole reactionRoleEntity = reactionRole.get();
                Role role = event.getGuild().getRoleById(reactionRoleEntity.getId());

                if (role != null) {
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                }
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!BotConfig.isModuleActive("slashcommands")) return;

        // Only accept commands from guilds
        if (!event.isFromGuild() && event.getMember() != null) return;

        event.deferReply(true).queue();

        Main.getInstance().getCommandManager().perform(Objects.requireNonNull(event.getMember()), event.getGuild(), null, null, event.getGuildChannel(), event).subscribe(x -> {
            if (x != null) {
                Main.getInstance().getCommandManager().timeoutUser(event.getUser());
            }
        });
    }
}
