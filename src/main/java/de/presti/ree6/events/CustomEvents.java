package de.presti.ree6.events;

import de.presti.ree6.module.actions.customevents.container.CustomEventContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.custom.CustomEventAction;
import de.presti.ree6.sql.entities.custom.CustomEventTyp;
import de.presti.ree6.utils.data.CustomEventMapper;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEvents implements EventListener {

    /**
     * The cache for the CustomEvents.
     */
    Map<CustomEventTyp, List<CustomEventContainer>> cache = new HashMap<>();

    /**
     * The last time the cache was checked.
     */
    long lastCheck;

    /**
     * Listens for all events.
     *
     * @param event The event.
     */
    @Override
    public void onEvent(@NotNull GenericEvent event) {

        if (event instanceof GenericGuildEvent genericGuildEvent) {
            runCacheEntry(genericGuildEvent.getClass(), genericGuildEvent.getGuild().getIdLong());
        }

        if (event instanceof GenericRoleEvent genericRoleEvent) {
            runCacheEntry(genericRoleEvent.getClass(), genericRoleEvent.getGuild().getIdLong());
        }

        if (event instanceof GenericMessageEvent genericMessageEvent) {
            if (!((GenericMessageEvent) event).isFromGuild()) return;

            runCacheEntry(genericMessageEvent.getClass(), genericMessageEvent.getGuild().getIdLong());
        }
    }

    /**
     * Run the cache entry for the given event.
     *
     * @param clazz   The event class.
     * @param guildId The guild id.
     */
    public void runCacheEntry(Class<? extends GenericEvent> clazz, long guildId) {
        cache.entrySet().stream().
                filter(c -> c.getKey() == CustomEventMapper.getEventTyp(clazz))
                .forEach(entry -> entry.getValue().forEach(CustomEventContainer::runActions));

        checkForNew(clazz, guildId);
    }

    /**
     * Check for new CustomEvents.
     *
     * @param clazz   The event class.
     * @param guildId The guild id.
     */
    public void checkForNew(Class<? extends GenericEvent> clazz, long guildId) {
        if (System.currentTimeMillis() - lastCheck < Duration.ofMinutes(1).toMillis()) return;
        lastCheck = System.currentTimeMillis();
        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new CustomEventAction(),
                "FROM CustomEventAction WHERE guildId=:gid",
                Map.of("gid", guildId)).map(x -> x.stream().map(CustomEventContainer::new).toList()).subscribe(list -> {
            CustomEventTyp typ = CustomEventMapper.getEventTyp(clazz);

            list.forEach(c -> {
                if (cache.containsKey(typ) && !cache.get(typ).contains(c)) {
                    cache.get(typ).add(c);
                } else {
                    cache.put(typ, List.of(c));
                }

                cache.values().forEach(entries -> entries.removeIf(z -> !list.contains(z)));
            });
        });

    }
}
