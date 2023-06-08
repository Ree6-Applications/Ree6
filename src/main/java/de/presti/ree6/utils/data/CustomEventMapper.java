package de.presti.ree6.utils.data;

import de.presti.ree6.sql.entities.custom.CustomEventTyp;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;

import java.util.HashMap;
import java.util.Map;

public class CustomEventMapper {

    public static Map<Class<? extends GenericEvent>, CustomEventTyp> map = new HashMap<>();

    public static void load() {
        map.put(GuildMemberJoinEvent.class, CustomEventTyp.MEMBER_JOIN);
        map.put(GuildMemberRemoveEvent.class, CustomEventTyp.MEMBER_LEAVE);
        map.put(GuildVoiceUpdateEvent.class, CustomEventTyp.VOICE_MOVE);
        map.put(MessageReceivedEvent.class, CustomEventTyp.MESSAGE);
        map.put(MessageDeleteEvent.class, CustomEventTyp.MESSAGE_DELETE);
        map.put(MessageUpdateEvent.class, CustomEventTyp.MESSAGE_UPDATE);
        map.put(MessageReactionAddEvent.class, CustomEventTyp.REACTION_ADD);
        map.put(MessageReactionRemoveEvent.class, CustomEventTyp.REACTION_REMOVE);
        map.put(GuildBanEvent.class, CustomEventTyp.MEMBER_BAN);
        map.put(GuildUnbanEvent.class, CustomEventTyp.MEMBER_UNBAN);
        map.put(RoleCreateEvent.class, CustomEventTyp.ROLE_CREATE);
        map.put(RoleDeleteEvent.class, CustomEventTyp.ROLE_DELETE);
        map.put(RoleUpdateColorEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(RoleUpdateHoistedEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(RoleUpdateMentionableEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(RoleUpdateNameEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(RoleUpdatePermissionsEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(RoleUpdateIconEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(RoleUpdatePositionEvent.class, CustomEventTyp.ROLE_UPDATE);
        map.put(ChannelCreateEvent.class, CustomEventTyp.CHANNEL_CREATE);
        map.put(ChannelDeleteEvent.class, CustomEventTyp.CHANNEL_DELETE);
        map.put(ChannelUpdateNameEvent.class, CustomEventTyp.CHANNEL_UPDATE);
        map.put(ChannelUpdateNSFWEvent.class, CustomEventTyp.CHANNEL_UPDATE);
    }

    public static CustomEventTyp getEventTyp(Class<? extends GenericEvent> clazz) {
        return map.get(clazz);
    }

    public static Class<? extends GenericEvent> getClass(CustomEventTyp typ) {
        for (Map.Entry<Class<? extends GenericEvent>, CustomEventTyp> entry : map.entrySet()) {
            if (entry.getValue() == typ) {
                return entry.getKey();
            }
        }
        return null;
    }

}
