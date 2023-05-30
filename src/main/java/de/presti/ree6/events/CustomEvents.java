package de.presti.ree6.events;

import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.custom.CustomEventAction;
import de.presti.ree6.sql.entities.custom.CustomEventTyp;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomEvents implements EventListener {

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildMemberJoinEvent guildMemberJoinEvent) {
            // TODO:: make some sort of a cache system to not fully tank the performance.
            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new CustomEventAction(), "SELECT * FROM CustomEvents WHERE guild=:gid AND event=:event",
                    Map.of("gid", guildMemberJoinEvent.getGuild().getIdLong(), "event", CustomEventTyp.MEMBER_JOIN)).forEach(customEventAction -> {
                        // TODO:: implement
            });
        }
    }
}
