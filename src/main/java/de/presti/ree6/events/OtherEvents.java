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
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * Event Handler for no categorized Events.
 */
@Slf4j
public class OtherEvents extends ListenerAdapter {

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
        SQLSession.getSqlConnector().getSqlWorker().createSettings(event.getGuild().getIdLong());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().deleteAllData(event.getGuild().getIdLong());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getId())).thenAccept(channelStats -> {
            if (channelStats != null) {
                if (channelStats.getMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.overallMembers") + ": " + event.getGuild().getMemberCount()).queue();
                    }
                }

                event.getGuild().loadMembers().onSuccess(members -> {
                    if (channelStats.getRealMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getRealMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.realMembers") + ": " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                        }
                    }

                    if (channelStats.getBotMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getBotMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.botMembers") + ": " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                        }
                    }
                });
            }
        });

        GuildUtil.handleMemberJoin(event.getGuild(), event.getMember());

        SQLSession.getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getIdLong()).thenAccept(x -> {
            if (x) {
                WebhookMessageBuilder wmb = new WebhookMessageBuilder();

                wmb.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                wmb.setUsername("Welcome!");

                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "message_join").thenAccept(messageSetting -> {
                    final String messageContent = messageSetting.getStringValue()
                            .replace("%user_name%", event.getMember().getUser().getName())
                            .replace("%guild_name%", event.getGuild().getName())
                            .replace("%guild_member_count%", String.valueOf(event.getGuild().getMemberCount()));

                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "message_join_image").thenAccept(joinImage -> {
                        if (!joinImage.getStringValue().isBlank()) {
                            try {
                                wmb.addFile("welcome.png", ImageCreationUtility.createJoinImage(event.getUser(), joinImage.getStringValue(),
                                        messageContent.replace("%user_mention%", event.getMember().getUser().getName())));
                            } catch (IOException e) {
                                wmb.setContent(messageContent);
                                log.error("Error while creating join image!", e);
                            }
                        } else {
                            wmb.setContent(messageContent.replace("%user_mention%", event.getMember().getUser().getAsMention()));
                        }

                        SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                            if (webhook == null) return;

                            WebhookUtil.sendWebhook(wmb.build(), webhook);
                        });
                    });

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

        SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getId())).thenAccept(channelStats -> {
            if (channelStats != null) {
                if (channelStats.getMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.overallMembers") + ": " + event.getGuild().getMemberCount()).queue();
                    }
                }

                event.getGuild().loadMembers().onSuccess(members -> {
                    if (channelStats.getRealMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getRealMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.realMembers") + ": " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                        }
                    }

                    if (channelStats.getBotMemberStatsChannelId() != null) {
                        GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getBotMemberStatsChannelId());
                        if (guildChannel != null) {
                            guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(), "label.botMembers") + ": " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                        }
                    }
                });
            }
        });

        if (BotConfig.isModuleActive("tickets")) {
            SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong())).thenAccept(tickets -> {
                if (tickets != null) {
                    Category category = event.getGuild().getCategoryById(tickets.getTicketCategory());

                    if (category != null) {
                        List<TextChannel> channels = category.getTextChannels().stream().filter(c -> c.getTopic() != null && c.getTopic().equalsIgnoreCase(event.getUser().getId())).toList();
                        if (!channels.isEmpty()) {
                            TextChannel channel = channels.get(0);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(BotConfig.getBotName())
                                    .append(" Ticket transcript")
                                    .append(" ")
                                    .append(ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)))
                                    .append("\n")
                                    .append("\n");


                            for (Message message : channel.getIterableHistory().reverse()) {
                                stringBuilder
                                        .append("[")
                                        .append(message.getTimeCreated().toZonedDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
                                        .append("]")
                                        .append(" ")
                                        .append(message.getAuthor().getAsTag())
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

                            stringBuilder.append("\n").append("Closed by").append(" ").append(event.getUser().getEffectiveName());

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            webhookMessageBuilder.setUsername(BotConfig.getBotName() + "-Tickets");

                            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                            webhookEmbedBuilder.setDescription("Here is the transcript of the ticket " + tickets.getTicketCount() + "!");
                            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                            webhookEmbedBuilder.setColor(BotWorker.randomEmbedColor().getRGB());

                            webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());
                            webhookMessageBuilder.addFile(tickets.getTicketCount() + "_transcript.txt", stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

                            WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), tickets.getLogChannelId(), tickets.getLogChannelWebhookToken(), false);
                            channel.delete().queue();
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

            if (!ArrayUtil.voiceJoined.containsKey(event.getMember()) && !event.getEntity().getUser().isBot()) {
                GuildVoiceState voiceState = event.getVoiceState();

                if (voiceState.isMuted() && BotConfig.shouldResetOnMute()) return;
                if (voiceState.isGuildMuted() && BotConfig.shouldResetOnMuteGlobal()) return;
                if (voiceState.isDeafened() && BotConfig.shouldResetOnDeafen()) return;
                if (voiceState.isGuildDeafened() && BotConfig.shouldResetOnDeafenGlobal()) return;

                ArrayUtil.voiceJoined.put(event.getMember(), System.currentTimeMillis());
            }

            if (BotConfig.isModuleActive("temporalvoice")) {
                checkCreationChannel(event.getGuild(), event.getMember(), event.getChannelJoined().getIdLong());
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
                .thenAccept(temporalVoicechannel -> {
                    if (temporalVoicechannel != null) {
                        VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);

                        if (voiceChannel == null)
                            return;

                        if (temporalVoicechannel.getGuildChannelId().getChannelId() != voiceChannel.getIdLong()) {
                            return;
                        }

                        if (voiceChannel.getParentCategory() != null) {
                            LanguageService.getByGuild(guild, "label.temporalVoiceName", "SPLIT").thenAccept(preName -> {
                                preName = preName.split("SPLIT")[0];
                                String finalPreName = preName;
                                LanguageService.getByGuild(guild, "label.temporalVoiceName",
                                                guild.getVoiceChannels().stream().filter(c -> c.getName().startsWith(finalPreName)).count() + 1)
                                        .thenAccept(name -> voiceChannel.getParentCategory().createVoiceChannel(name)
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

            SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(member.getGuild().getIdLong(), member.getIdLong()).thenAccept(x -> {
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
            SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "configuration_autopublish").thenAccept(x -> {
                if (x.getBooleanValue())
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

            ModerationUtil.shouldModerate(event.getGuild().getIdLong()).thenAccept(x -> {
                AtomicBoolean moderated = new AtomicBoolean(false);
                if (x) {
                    ModerationUtil.checkMessage(event.getGuild().getIdLong(), event.getMessage().getContentRaw()).thenAccept(y -> {
                        if (y) {
                            Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);

                            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.blacklisted"), event.getChannel(), null);
                            moderated.set(true);
                        }
                    });
                }

                if (moderated.get()) return;

                Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), event.getMessage().getContentRaw(), event.getMessage(), event.getGuildChannel(), null).thenAccept(value -> {
                    if (!value) {
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
                            if (!ArrayUtil.timeout.contains(event.getMember())) {

                                SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(event.getGuild().getIdLong(), event.getMember().getIdLong()).thenAccept(userLevel -> {
                                    if (userLevel.addExperience(RandomUtils.random.nextInt(15, 26))) {
                                        SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "level_message").thenAccept(z -> {
                                            if (z.getBooleanValue()) {
                                                Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(),
                                                        "message.levelUp", userLevel.getLevel(), LanguageService.getByGuild(event.getGuild(), "label.chat")
                                                        , event.getMember().getAsMention()), event.getChannel());
                                            }
                                        });
                                    }

                                    SQLSession.getSqlConnector().getSqlWorker().addChatLevelData(event.getGuild().getIdLong(), userLevel);

                                    ArrayUtil.timeout.add(event.getMember());

                                    ThreadUtil.createThread(y -> ArrayUtil.timeout.remove(event.getMember()), Duration.ofSeconds(30), false, false);
                                });
                            }

                            GuildUtil.handleChatLevelReward(event.getGuild(), event.getMember());
                        }
                    } else {
                        Main.getInstance().getCommandManager().timeoutUser(event.getAuthor());
                    }
                });
            });
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!BotConfig.isModuleActive("reactionRoles")) return;

        if (event.getMember() == null) return;

        if (!PermissionUtil.checkPermission(event.getGuild().getSelfMember(), Permission.MESSAGE_HISTORY)) return;

        event.retrieveMessage().queue(message -> {

            EmojiUnion emojiUnion = event.getReaction().getEmoji();

            String reactionCode = emojiUnion.getAsReactionCode();

            long emojiId;
            if (event.getReaction().getEmoji().getType() == Emoji.Type.CUSTOM) {
                emojiId = Long.parseLong(reactionCode.split(":")[1]);
            } else {
                emojiId = reactionCode.replace(":", "").hashCode();
            }

            if (message.getAuthor().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                String messageContent = message.getContentRaw();

                LanguageService.getByGuild(event.getGuild(), "message.reactions.reactionNeeded", "SPLIT_HERE").thenAccept(translation -> {
                    if (messageContent.startsWith(translation.split("SPLIT_HERE")[0])) {
                        if (event.getMember().hasPermission(Permission.ADMINISTRATOR) && message.getMessageReference() != null) {
                            if (message.getMentions().getRoles().isEmpty()) {
                                LanguageService.getByGuild(event.getGuild(), "message.reactions.roleNotFound").thenApply(message::editMessage).thenAccept(MessageEditAction::queue);
                                return;
                            }

                            Role role = message.getMentions().getRoles().get(0);

                            if (role == null) {
                                LanguageService.getByGuild(event.getGuild(), "message.reactions.roleNotFound").thenApply(message::editMessage).thenAccept(MessageEditAction::queue);
                                return;
                            }

                            ReactionRole reactionRole = new ReactionRole(event.getGuild().getIdLong(), emojiId, emojiUnion.getFormatted(), role.getIdLong(), message.getMessageReference().getMessageIdLong());
                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole).join();

                            if (message.getMessageReference().getMessage() != null) {
                                message.getMessageReference().getMessage().addReaction(event.getEmoji()).queue();
                            }

                            LanguageService.getByGuild(event.getGuild(), "message.reactions.roleAssign", role.getAsMention())
                                    .thenAccept(x -> message.editMessage(x).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue());
                        }
                    }
                });
            } else {
                SQLSession.getSqlConnector().getSqlWorker()
                        .getEntity(new ReactionRole(), "FROM ReactionRole WHERE guildRoleId.guildId=:gid AND emoteId=:emoteId AND messageId=:messageId",
                                Map.of("gid", event.getGuild().getIdLong(), "emoteId", emojiId, "messageId", message.getIdLong())).thenAccept(reactionRole -> {
                            if (reactionRole != null) {
                                Role role = event.getGuild().getRoleById(reactionRole.getId());

                                if (role != null) {
                                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                                }

                                boolean changes = false;

                                if (reactionRole.getChannelId() == 0) {
                                    reactionRole.setChannelId(event.getChannel().getIdLong());
                                    changes = true;
                                }

                                if (reactionRole.getFormattedEmote().isBlank()) {
                                    reactionRole.setFormattedEmote(emojiUnion.getFormatted());
                                    changes = true;
                                }

                                if (changes)
                                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole).join();
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
                Map.of("gid", event.getGuild().getIdLong(), "emoteId", emojiId, "messageId", event.getMessageIdLong())).thenAccept(reactionRole -> {
            if (reactionRole != null) {
                Role role = event.getGuild().getRoleById(reactionRole.getId());

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

        Main.getInstance().getCommandManager().perform(Objects.requireNonNull(event.getMember()), event.getGuild(), null, null, event.getGuildChannel(), event).thenAccept(x -> {
            if (x != null) {
                Main.getInstance().getCommandManager().timeoutUser(event.getUser());
            }
        });
    }
}
