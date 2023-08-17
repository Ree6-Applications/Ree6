package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.audio.AudioPlayerReceiveHandler;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.ReactionRole;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.entities.Tickets;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.sql.entities.level.VoiceUserLevel;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.apis.ChatGPTAPI;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.data.ImageCreationUtility;
import de.presti.ree6.utils.others.*;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
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

        BotWorker.setActivity(event.getJDA(), Data.getStatus(), Activity.ActivityType.PLAYING);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().createSettings(event.getGuild().getId());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().deleteAllData(event.getGuild().getId());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        ChannelStats channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getId()));
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

        AutoRoleHandler.handleMemberJoin(event.getGuild(), event.getMember());

        if (!SQLSession.getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId())) return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wmb.setUsername("Welcome!");

        String messageContent = SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "message_join")
                .getStringValue()
                .replace("%user_name%", event.getMember().getUser().getName())
                .replace("%guild_name%", event.getGuild().getName())
                .replace("%guild_member_count%", String.valueOf(event.getGuild().getMemberCount()));
        if (!SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "message_join_image").getStringValue().isBlank()) {
            try {
                messageContent = messageContent.replace("%user_mention%", event.getMember().getUser().getName());
                wmb.addFile("welcome.png", ImageCreationUtility.createJoinImage(event.getUser(),
                        SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "message_join_image").getStringValue(), messageContent));
            } catch (IOException e) {
                wmb.setContent(messageContent);
                log.error("Error while creating join image!", e);
            }
        } else {
            messageContent = messageContent.replace("%user_mention%", event.getMember().getUser().getAsMention());
            wmb.setContent(messageContent);
        }

        WebhookUtil.sendWebhook(wmb.build(), SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getId()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        super.onGuildMemberRemove(event);

        ChannelStats channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE guildId=:gid", Map.of("gid", event.getGuild().getId()));
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

        if (Data.isModuleActive("tickets")) {
            Tickets tickets = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", event.getGuild().getIdLong()));
            if (tickets != null) {
                Category category = event.getGuild().getCategoryById(tickets.getTicketCategory());

                if (category != null) {
                    List<TextChannel> channels = category.getTextChannels().stream().filter(c -> c.getTopic() != null && c.getTopic().equalsIgnoreCase(event.getUser().getId())).toList();
                    if (!channels.isEmpty()) {
                        TextChannel channel = channels.get(0);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(Data.getBotName())
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
                        webhookMessageBuilder.setUsername(Data.getBotName() + "-Tickets");

                        WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                        webhookEmbedBuilder.setDescription("Here is the transcript of the ticket " + tickets.getTicketCount() + "!");
                        webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
                        webhookEmbedBuilder.setColor(BotWorker.randomEmbedColor().getRGB());

                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());
                        webhookMessageBuilder.addFile(tickets.getTicketCount() + "_transcript.txt", stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

                        WebhookUtil.sendWebhook(null, webhookMessageBuilder.build(), tickets.getLogChannelId(), tickets.getLogChannelWebhookToken(), false);
                        channel.delete().queue();
                    }
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
            if (!ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
                ArrayUtil.voiceJoined.put(event.getMember().getUser(), System.currentTimeMillis());
            }

            if (Data.isModuleActive("temporalvoice")) {
                TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildId=:gid", Map.of("gid", event.getGuild().getId()));

                if (temporalVoicechannel != null) {
                    VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getChannelJoined().getId());

                    if (voiceChannel == null)
                        return;

                    if (!temporalVoicechannel.getVoiceChannelId().equalsIgnoreCase(voiceChannel.getId())) {
                        return;
                    }

                    if (voiceChannel.getParentCategory() != null) {
                        String preName = LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName", "SPLIT");
                        preName = preName.split("SPLIT")[0];

                        String finalPreName = preName;
                        voiceChannel.getParentCategory().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName",
                                event.getGuild().getVoiceChannels().stream().filter(c -> c.getName().startsWith(finalPreName)).count() + 1)).queue(channel -> {
                            event.getGuild().moveVoiceMember(event.getMember(), channel).queue();
                            ArrayUtil.temporalVoicechannel.add(channel.getId());
                        });
                    }
                }
            }
        } else if (event.getChannelJoined() == null) {
            if (ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
                int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(event.getMember().getUser())));

                int addXP = 0;

                for (int i = 1; i <= min; i++) {
                    addXP += RandomUtils.random.nextInt(5, 11);
                }

                VoiceUserLevel newUserLevel = SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(event.getGuild().getId(), event.getMember().getId());
                newUserLevel.addExperience(addXP);

                SQLSession.getSqlConnector().getSqlWorker().addVoiceLevelData(event.getGuild().getId(), newUserLevel);

                AutoRoleHandler.handleVoiceLevelReward(event.getGuild(), event.getMember());
            }

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

            if (Data.isModuleActive("temporalvoice")) {
                if (ArrayUtil.isTemporalVoicechannel(event.getChannelLeft())
                        && (event.getChannelLeft().getMembers().isEmpty() || (event.getChannelLeft().getMembers().size() == 1 &&
                        event.getChannelLeft().getMembers().get(0).getIdLong() == event.getJDA().getSelfUser().getIdLong()))) {
                    event.getChannelLeft().delete().queue(c -> ArrayUtil.temporalVoicechannel.remove(event.getChannelLeft().getId()));
                }
            }
        } else {

            TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildId=:gid", Map.of("gid", event.getGuild().getId()));

            if (temporalVoicechannel != null) {
                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getChannelJoined().getId());

                if (voiceChannel == null)
                    return;

                if (!temporalVoicechannel.getVoiceChannelId().equalsIgnoreCase(voiceChannel.getId())) {
                    return;
                }

                if (voiceChannel.getParentCategory() != null) {
                    String preName = LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName", "SPLIT");
                    preName = preName.split("SPLIT")[0];

                    String finalPreName = preName;
                    voiceChannel.getParentCategory().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName",
                            event.getGuild().getVoiceChannels().stream().filter(c -> c.getName().startsWith(finalPreName)).count() + 1)).queue(channel -> {
                        event.getGuild().moveVoiceMember(event.getMember(), channel).queue();
                        ArrayUtil.temporalVoicechannel.add(channel.getId());
                    });
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {
        if (event.getMember() != event.getGuild().getSelfMember()) return;

        if (!event.isGuildDeafened()) {
            event.getGuild().getSelfMember().deafen(true).queue();
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
                Data.isModuleActive("autopublish") &&
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "configuration_autopublish").getBooleanValue()) {
            event.getMessage().crosspost().queue(c -> c.addReaction(Emoji.fromUnicode("U+1F4E2")).queue());
        }

        if (event.isFromGuild() && (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.VOICE)) && event.getMember() != null) {

            if (event.getAuthor().isBot()) return;

            if (ModerationUtil.shouldModerate(event.getGuild().getId())) {
                if (ModerationUtil.checkMessage(event.getGuild().getId(), event.getMessage().getContentRaw())) {
                    Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.blacklisted"), event.getChannel(), null);
                    return;
                } /* else if (!event.getMessage().getAttachments().isEmpty()) {
                    for (Message.Attachment attachment : event.getMessage().getAttachments()) {
                        if (attachment.isImage()) {
                            if (ModerationUtil.checkImage(event.getGuild().getId(), attachment.getUrl())) {
                                Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                                Main.getInstance().getCommandManager().sendMessage("The Image contained blacklisted words!", event.getChannel(), null);
                                return;
                            }
                        }
                    }
                } else {
                    String messageContent = event.getMessage().getContentRaw();
                    String extractedUrl = ModerationUtil.extractUrl(messageContent);

                    if (extractedUrl != null &&!extractedUrl.isEmpty()) {
                        if (ModerationUtil.checkImage(event.getGuild().getId(), extractedUrl)) {
                            Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                            Main.getInstance().getCommandManager().sendMessage("The Image contained blacklisted words!", event.getChannel(), null);
                            return;
                        }
                    }
                } */
            }

            if (!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage());
            }

            if (!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
            }

            if (!Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), event.getMessage().getContentRaw(), event.getMessage(), event.getChannel(), null)) {

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

                if (Data.isModuleActive("level")) {
                    if (!ArrayUtil.timeout.contains(event.getMember())) {

                        ChatUserLevel userLevel = SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(event.getGuild().getId(), event.getMember().getId());

                        if (userLevel.addExperience(RandomUtils.random.nextInt(15, 26)) && SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "level_message").getBooleanValue()) {
                            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(),
                                    "message.levelUp", userLevel.getLevel(), LanguageService.getByGuild(event.getGuild(), "label.chat")
                                    , event.getMember().getAsMention()), event.getChannel());
                        }

                        SQLSession.getSqlConnector().getSqlWorker().addChatLevelData(event.getGuild().getId(), userLevel);

                        ArrayUtil.timeout.add(event.getMember());

                        ThreadUtil.createThread(x -> ArrayUtil.timeout.remove(event.getMember()), null, Duration.ofSeconds(30), false, false);
                    }

                    AutoRoleHandler.handleChatLevelReward(event.getGuild(), event.getMember());
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!Data.isModuleActive("reactionRoles")) return;

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

                if (messageContent.startsWith(LanguageService.getByGuild(event.getGuild(), "message.reactions.reactionNeeded", "SPLIT_HERE").split("SPLIT_HERE")[0])) {
                    if (event.getMember().hasPermission(Permission.ADMINISTRATOR) && message.getMessageReference() != null) {
                        if (message.getMentions().getRoles().isEmpty()) {
                            message.editMessage(LanguageService.getByGuild(event.getGuild(), "message.reactions.roleNotFound")).queue();
                            return;
                        }

                        Role role = message.getMentions().getRoles().get(0);

                        if (role == null) {
                            message.editMessage(LanguageService.getByGuild(event.getGuild(), "message.reactions.roleNotFound")).queue();
                            return;
                        }

                        ReactionRole reactionRole = new ReactionRole(event.getGuild().getIdLong(), emojiId, emojiUnion.getFormatted(), role.getIdLong(), message.getMessageReference().getMessageIdLong());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole);

                        if (message.getMessageReference().getMessage() != null) {
                            message.getMessageReference().getMessage().addReaction(event.getEmoji()).queue();
                        }

                        message.editMessage(LanguageService.getByGuild(event.getGuild(), "message.reactions.roleAssign", role.getAsMention()))
                                .delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
                    }
                }
            } else {
                ReactionRole reactionRole = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(), "FROM ReactionRole WHERE guildId=:gid AND emoteId=:emoteId AND messageId=:messageId", Map.of("gid", event.getGuild().getIdLong(), "emoteId", emojiId, "messageId", message.getIdLong()));

                if (reactionRole != null) {
                    Role role = event.getGuild().getRoleById(reactionRole.getRoleId());

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
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole);
                }
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!Data.isModuleActive("reactionRoles")) return;
        if (event.getMember() == null) return;

        String reactionCode = event.getReaction().getEmoji().getAsReactionCode();

        long emojiId;
        if (event.getReaction().getEmoji().getType() == Emoji.Type.CUSTOM) {
            emojiId = Long.parseLong(reactionCode.split(":")[1]);
        } else {
            emojiId = reactionCode.replace(":", "").hashCode();
        }

        ReactionRole reactionRole = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(),
                "FROM ReactionRole WHERE guildId=:gid AND emoteId=:emoteId AND messageId=:messageId",
                Map.of("gid", event.getGuild().getIdLong(), "emoteId", emojiId, "messageId", event.getMessageIdLong()));

        if (reactionRole != null) {
            Role role = event.getGuild().getRoleById(reactionRole.getRoleId());

            if (role != null) {
                event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!Data.isModuleActive("slashcommands")) return;

        // Only accept commands from guilds
        if (!event.isFromGuild() && event.getMember() != null) return;

        event.deferReply(true).queue();

        Main.getInstance().getCommandManager().perform(Objects.requireNonNull(event.getMember()), event.getGuild(), null, null, event.getChannel(), event);
    }
}
