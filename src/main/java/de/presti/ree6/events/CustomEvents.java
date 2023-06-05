package de.presti.ree6.events;

import de.presti.ree6.actions.customevents.container.CustomEventContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.custom.CustomEventAction;
import de.presti.ree6.sql.entities.custom.CustomEventTyp;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEvents implements EventListener {

    Map<GenericEvent, List<CustomEventContainer>> cache = new HashMap<>();

    @Override
    public void onEvent(@NotNull GenericEvent event) {

        if (event instanceof GuildMemberJoinEvent guildMemberJoinEvent) {
            // TODO:: implement

            cache.entrySet().stream().filter(c -> c.getKey() instanceof GuildMemberJoinEvent).forEach(entry -> entry.getValue().forEach(c -> {
                if (c.getExtraArgument().equalsIgnoreCase(guildMemberJoinEvent.getMember().getId())) {
                    c.runActions();
                }
            }));

            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new CustomEventAction(), "SELECT * FROM CustomEvents WHERE guild=:gid AND event=:event",
                    Map.of("gid", guildMemberJoinEvent.getGuild().getIdLong(), "event", CustomEventTyp.MEMBER_JOIN)).stream().map(CustomEventContainer::new).forEach(c -> {
                if (cache.containsKey(event) && !cache.get(event).contains(c)) {
                    cache.get(event).add(c);
                } else {
                    cache.put(event, List.of(c));
                }
            });
        }
    }
}
