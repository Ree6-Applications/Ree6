package de.presti.ree6.module.notifications.impl;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import io.github.redouane59.twitter.dto.user.UserV2;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.*;

@Slf4j
public class TwitterSonic implements ISonic {

    ArrayList<SonicIdentifier> twitterChannels = new ArrayList<>();

    @Override
    public void load(List<ChannelStats> channelStats) {
        try {
            channelStats.stream().map(ChannelStats::getTwitterFollowerChannelUsername).filter(Objects::nonNull).forEach(this::add);
            load();
        } catch (Exception exception) {
            log.error("Error while loading Twitter data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public void load() {
        // Register all Twitter Users.
        SQLSession.getSqlConnector().getSqlWorker().getAllTwitterNames().subscribe(twitterNames ->
                twitterNames.forEach(this::add));
    }

    @Override
    public List<SonicIdentifier> getList() {
        return twitterChannels;
    }

    @Override
    public void run() {
        for (String twitterName : getList().stream().map(SonicIdentifier::getIdentifier).toList()) {
            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                    "FROM ChannelStats WHERE twitterFollowerChannelUsername=:name", Map.of("name", twitterName)).subscribe(channelStats -> {
                if (!channelStats.isEmpty()) {
                    UserV2 twitterUser;
                    try {
                        twitterUser = Main.getInstance().getNotifier().getTwitterClient().getUserFromUserName(twitterName);
                    } catch (NoSuchElementException e) {
                        return;
                    }

                    if (twitterUser.getData() == null) return;

                    for (ChannelStats channelStat : channelStats) {
                        if (channelStat.getTwitterFollowerChannelUsername() != null) {
                            GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getTwitchFollowerChannelId());
                            if (guildChannel == null) continue;

                            LanguageService.getByGuild(guildChannel.getGuild(), "label.twitterCountName", twitterUser.getFollowersCount()).subscribe(newName -> {
                                if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                    return;

                                if (!guildChannel.getName().equalsIgnoreCase(newName)) {
                                    guildChannel.getManager().setName(newName).queue();
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void unload() {

    }

    @Override
    public void remove(SonicIdentifier object) {
        if (Main.getInstance().getNotifier().getTwitterClient() == null) return;
        if (!contains(object)) return;

        SQLSession.getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(object.getIdentifier()).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE twitterFollowerChannelUsername=:name",
                    Map.of("name", object.getIdentifier())).subscribe(channelStats -> {
                if (channelStats.isPresent()) return;

                twitterChannels.remove(object);
            });
        });
    }
}
