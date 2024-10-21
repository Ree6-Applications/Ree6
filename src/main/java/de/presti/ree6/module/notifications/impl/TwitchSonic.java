package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import club.minnced.discord.webhook.send.component.button.Button;
import club.minnced.discord.webhook.send.component.layout.ActionRow;
import com.github.twitch4j.events.ChannelFollowCountUpdateEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.GameList;
import com.github.twitch4j.helix.domain.User;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.awt.*;
import java.util.List;
import java.util.*;

@Slf4j
public class TwitchSonic implements ISonic {

    ArrayList<SonicIdentifier> twitchChannels = new ArrayList<>();

    @Override
    public void load(List<ChannelStats> channelStats) {
        try {
            channelStats.stream().map(ChannelStats::getTwitchFollowerChannelUsername).filter(Objects::nonNull).forEach(this::add);
            load();
        } catch (Exception exception) {
            log.error("Error while loading Twitch data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public void load() {
        // Register all Twitch Channels.
        SQLSession.getSqlConnector().getSqlWorker().getAllTwitchNames().subscribe(channel ->
                channel.forEach(this::add));
        // Register the Event-handler.
        run();
    }

    @Override
    public List<SonicIdentifier> getList() {
        return twitchChannels;
    }

    @Override
    public void run() {
        Main.getInstance().getNotifier().getTwitchClient().getEventManager().onEvent(ChannelGoLiveEvent.class, channelGoLiveEvent -> SQLSession.getSqlConnector().getSqlWorker().getTwitchWebhooksByName(channelGoLiveEvent.getChannel().getName()).subscribe(webhooks -> {
            if (webhooks.isEmpty()) {
                return;
            }

            String twitchUrl = "https://twitch.tv/" + channelGoLiveEvent.getChannel().getName();

            // Create a Webhook Message.
            WebhookMessageBuilder wmb = new WebhookMessageBuilder();

            wmb.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());
            wmb.setUsername(BotConfig.getBotName());

            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(channelGoLiveEvent.getStream().getUserName(), twitchUrl));
            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitch Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl(), null));

            // Try getting the User.
            Optional<User> twitchUserRequest = Main.getInstance().getNotifier().getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(channelGoLiveEvent.getStream().getUserName())).execute().getUsers().stream().findFirst();
            if (twitchUserRequest.isPresent()) {
                webhookEmbedBuilder.setThumbnailUrl(twitchUserRequest.orElseThrow().getProfileImageUrl());
            }

            // Set rest of the Information.
            webhookEmbedBuilder.setDescription("**" + channelGoLiveEvent.getStream().getTitle() + "**");
            GameList gameList = Main.getInstance().getNotifier().getTwitchClient().getClientHelper().getTwitchHelix().getGames(null, List.of(channelGoLiveEvent.getStream().getGameId()), null, null).execute();
            if (!gameList.getGames().isEmpty()) {
                webhookEmbedBuilder.setThumbnailUrl(gameList.getGames().get(0).getBoxArtUrl());
            }

            webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(channelGoLiveEvent.getStream().getUserName(), twitchUserRequest.map(User::getProfileImageUrl).orElse(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()), twitchUrl));
            webhookEmbedBuilder.setImageUrl(channelGoLiveEvent.getStream().getThumbnailUrlTemplate());
            webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(channelGoLiveEvent.getStream().getGameName(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()));
            webhookEmbedBuilder.setColor(Color.MAGENTA.darker().getRGB());

            wmb.addComponents(ActionRow.of(new Button(Button.Style.LINK, twitchUrl).setLabel(twitchUserRequest.isPresent() ? twitchUserRequest.get().getDisplayName() : "Watch Stream")));
            wmb.addEmbeds(webhookEmbedBuilder.build());

            // Go through every Webhook that is registered for the Twitch Channel
            webhooks.forEach(webhook -> {
                String message = webhook.getMessage()
                        .replace("%name%", channelGoLiveEvent.getStream().getUserName())
                        .replace("%url%", twitchUrl);
                wmb.setContent(message);
                WebhookUtil.sendWebhook(wmb.build(), webhook, WebhookUtil.WebhookTyp.TWITCH);
            });
        }));

        Main.getInstance().getNotifier().getTwitchClient().getEventManager().onEvent(ChannelFollowCountUpdateEvent.class, channelFollowCountUpdateEvent -> SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                "FROM ChannelStats WHERE LOWER(twitchFollowerChannelUsername) = :name",
                Map.of("name", channelFollowCountUpdateEvent.getChannel().getName().toLowerCase())).subscribe(channelStats -> {
            if (!channelStats.isEmpty()) {
                for (ChannelStats channelStat : channelStats) {
                    if (channelStat.getTwitchFollowerChannelId() != null) {
                        GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getTwitchFollowerChannelId());
                        if (guildChannel != null) {
                            if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                continue;

                            LanguageService.getByGuild(guildChannel.getGuild(), "label.twitchCountName", channelFollowCountUpdateEvent.getFollowCount()).subscribe(newName -> {
                                if (!guildChannel.getName().equalsIgnoreCase(newName)) {
                                    guildChannel.getManager().setName(newName).queue();
                                }
                            });
                        }
                    }
                }
            }
        }));
    }

    @Override
    public void unload() {
        twitchChannels.forEach(channel -> {
            Main.getInstance().getNotifier().getTwitchClient().getClientHelper().disableStreamEventListener(channel.getIdentifier());
            Main.getInstance().getNotifier().getTwitchClient().getClientHelper().disableFollowEventListener(channel.getIdentifier());
        });
    }

    @Override
    public void add(SonicIdentifier object) {
        if (Main.getInstance().getNotifier().getTwitchClient() == null) return;
        if (contains(object)) return;

        String twitchChannel = object.getIdentifier().toLowerCase();

        twitchChannels.add(new SonicIdentifier(twitchChannel));

        Main.getInstance().getNotifier().getTwitchClient().getClientHelper().enableStreamEventListener(twitchChannel);
        Main.getInstance().getNotifier().getTwitchClient().getClientHelper().enableFollowEventListener(twitchChannel);
    }

    @Override
    public boolean contains(String identifier) {
        return ISonic.super.contains(identifier.toLowerCase());
    }

    @Override
    public void remove(SonicIdentifier object) {
        if (Main.getInstance().getNotifier().getTwitchClient() == null) return;

        String twitchChannel = object.getIdentifier().toLowerCase();

        SQLSession.getSqlConnector().getSqlWorker().getTwitchWebhooksByName(twitchChannel).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE twitchFollowerChannelUsername=:name", Map.of("name", twitchChannel))
                    .subscribe(channelStats -> {
                        if (channelStats.isPresent()) return;

                        twitchChannels.remove(object);

                        Main.getInstance().getNotifier().getTwitchClient().getClientHelper().disableStreamEventListener(twitchChannel);
                        Main.getInstance().getNotifier().getTwitchClient().getClientHelper().disableFollowEventListener(twitchChannel);
                    });
        });
    }
}
