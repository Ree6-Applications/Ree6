package de.presti.ree6.events;

import de.presti.ree6.actions.customevents.container.CustomEventContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.custom.CustomEventAction;
import de.presti.ree6.sql.entities.custom.CustomEventTyp;
import de.presti.ree6.utils.data.CustomEventMapper;
import de.presti.ree6.utils.others.ThreadUtil;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEvents implements EventListener {

    Map<CustomEventTyp, List<CustomEventContainer>> cache = new HashMap<>();

    long lastCheck;

    @Override
    public void onEvent(@NotNull GenericEvent event) {

        if (event instanceof GenericGuildEvent genericGuildEvent) {
            cache.entrySet().stream().
                    filter(c -> c.getKey() == CustomEventMapper.getEventTyp(genericGuildEvent.getClass()))
                    .forEach(entry -> entry.getValue().forEach(CustomEventContainer::runActions));


            if (System.currentTimeMillis() - lastCheck < Duration.ofMinutes(1).toMillis()) return;
            lastCheck = System.currentTimeMillis();
            ThreadUtil.createThread(x -> {
                List<CustomEventContainer> list = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new CustomEventAction(), "SELECT * FROM CustomEvents WHERE guild=:gid",
                        Map.of("gid", genericGuildEvent.getGuild().getIdLong())).stream().map(CustomEventContainer::new).toList();

                list.forEach(c -> {

                    CustomEventTyp typ = CustomEventMapper.getEventTyp(genericGuildEvent.getClass());

                    if (cache.containsKey(typ) && !cache.get(typ).contains(c)) {
                        cache.get(typ).add(c);
                    } else {
                        cache.put(typ, List.of(c));
                    }
                });

                cache.values().forEach(entries -> entries.removeIf(c -> !list.contains(c)));
            });
        }
    }
}
