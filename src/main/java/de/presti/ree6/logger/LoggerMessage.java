package de.presti.ree6.logger;

import club.minnced.discord.webhook.send.WebhookMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class LoggerMessage {

    private long id = 0L;
    private String authCode = "";
    private boolean cancel = false;

    private WebhookMessage wem;

    private RoleData roleData;
    private Guild guild;
    private Member m;
    private VoiceChannel vc;
    private VoiceChannel vc2;
    private ArrayList<Role> addedRoles;
    private ArrayList<Role> removedRoles;

    private String nickname;
    private String nickname2;

    private LogTyp type;

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, ArrayList<Role> addedRoles, LogTyp type) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.addedRoles = addedRoles;
        this.type = type;
    }

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, ArrayList<Role> removedRoles, LogTyp type, boolean ignore) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.removedRoles = removedRoles;
        this.type = type;
    }

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, RoleData roleData, LogTyp type) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.roleData = roleData;
        this.type = type;
    }

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, Member m, LogTyp type) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.m = m;
        this.type = type;
    }

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, Member m, String nick, String nick2, LogTyp type) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.m = m;
        this.nickname = nick;
        this.nickname2 = nick2;
        this.type = type;
    }

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, Member m, LogTyp type, VoiceChannel vc) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.m = m;
        this.type = type;
        this.vc = vc;
    }

    public LoggerMessage(Guild g, long c, String auth, WebhookMessage wem, Member m, LogTyp type, VoiceChannel vc, VoiceChannel vc2) {
        this.guild = g;
        this.id = c;
        this.authCode = auth;
        this.wem = wem;
        this.m = m;
        this.type = type;
        this.vc = vc;
        this.vc2 = vc2;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public WebhookMessage getWem() {
        return wem;
    }

    public void setWem(WebhookMessage wem) {
        this.wem = wem;
    }

    public Member getM() {
        return m;
    }

    public void setM(Member m) {
        this.m = m;
    }

    public LogTyp getType() {
        return type;
    }

    public void setType(LogTyp type) {
        this.type = type;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public VoiceChannel getVc() {
        return vc;
    }

    public void setVc(VoiceChannel vc) {
        this.vc = vc;
    }

    public VoiceChannel getVc2() {
        return vc2;
    }

    public void setVc2(VoiceChannel vc2) {
        this.vc2 = vc2;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname2() {
        return nickname2;
    }

    public void setNickname2(String nickname2) {
        this.nickname2 = nickname2;
    }

    public RoleData getRoleData() {
        return roleData;
    }

    public void setRoleData(RoleData roleData) {
        this.roleData = roleData;
    }

    public ArrayList<Role> getAddedRoles() {
        return addedRoles;
    }

    public void setAddedRoles(ArrayList<Role> addedRoles) {
        this.addedRoles = addedRoles;
    }

    public ArrayList<Role> getRemovedRoles() {
        return removedRoles;
    }

    public void setRemovedRoles(ArrayList<Role> removedRoles) {
        this.removedRoles = removedRoles;
    }

    public enum LogTyp {
        VC_LEAVE, VC_JOIN, VC_MOVE, NICKNAME_CHANGE, ROLEDATA_CHANGE, MEMBERROLE_CHANGE, CHANNELDATA_CHANGE, ELSE
    }

    public static class RoleData {

        private String Id;
        private String newName;
        private String oldName;
        private Color newColor;
        private Color oldColor;
        private EnumSet<Permission> newPermissions;
        private EnumSet<Permission> oldPermissions;
        private boolean created;
        private boolean delete;
        private boolean mention;
        private boolean hoisted;

        private boolean mentionChanged;
        private boolean hoistedChanged;

        public RoleData(String Id, String oldName, boolean value, BooleanValueTyp typ) {
            this.Id = Id;
            this.oldName = oldName;
            if (typ == BooleanValueTyp.CREATE) this.created = value;
            if (typ == BooleanValueTyp.DELETE) this.delete = value;
            if (typ == BooleanValueTyp.MENTION) setMention(value);
            if (typ == BooleanValueTyp.HOISTED) setHoisted(value);
        }

        public RoleData(String Id, String oldName, String newName) {
            this.Id = Id;
            this.oldName = oldName;
            this.newName = newName;
        }

        public RoleData(String Id, Color oldColor, Color newColor) {
            this.Id = Id;
            this.oldColor = oldColor;
            this.newColor = newColor;
        }

        public RoleData(String Id, EnumSet<Permission> oldPermissions, EnumSet<Permission> newPermissions) {
            this.Id = Id;
            this.oldPermissions = oldPermissions;
            this.newPermissions = newPermissions;
        }

        public RoleData(String Id, String oldName, String newName, Color oldColor, Color newColor, EnumSet<Permission> oldPermissions, EnumSet<Permission> newPermissions, boolean value, BooleanValueTyp typ) {
            this.Id = Id;
            this.oldName = oldName;
            this.newName = newName;
            this.oldColor = oldColor;
            this.newColor = newColor;
            this.oldPermissions = oldPermissions;
            this.newPermissions = newPermissions;
            if (typ == BooleanValueTyp.CREATE) this.created = value;
            if (typ == BooleanValueTyp.DELETE) this.delete = value;
            if (typ == BooleanValueTyp.MENTION) setMention(value);
            if (typ == BooleanValueTyp.HOISTED) setHoisted(value);
        }

        public String getId() {
            return Id;
        }

        public void setId(String Id) {
            this.Id = Id;
        }

        public String getNewName() {
            return newName;
        }

        public void setNewName(String newName) {
            this.newName = newName;
        }

        public String getOldName() {
            return oldName;
        }

        public void setOldName(String oldName) {
            this.oldName = oldName;
        }

        public EnumSet<Permission> getNewPermissions() {
            return newPermissions;
        }

        public void setNewPermissions(EnumSet<Permission> newPermissions) {
            this.newPermissions = newPermissions;
        }

        public EnumSet<Permission> getOldPermissions() {
            return oldPermissions;
        }

        public void setOldPermissions(EnumSet<Permission> oldPermissions) {
            this.oldPermissions = oldPermissions;
        }

        public Color getNewColor() {
            return newColor;
        }

        public void setNewColor(Color newColor) {
            this.newColor = newColor;
        }

        public Color getOldColor() {
            return oldColor;
        }

        public void setOldColor(Color oldColor) {
            this.oldColor = oldColor;
        }

        public boolean isCreated() {
            return created;
        }

        public void setCreated(boolean created) {
            this.created = created;
        }

        public boolean isDelete() {
            return delete;
        }

        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        public boolean isMention() {
            return mention;
        }

        public void setMention(boolean mention) {
            this.mention = mention;
            mentionChanged = true;
        }

        public boolean isHoisted() {
            return hoisted;
        }

        public void setHoisted(boolean hoisted) {
            this.hoisted = hoisted;
            hoistedChanged = true;
        }

        public boolean isMentionChanged() {
            return mentionChanged;
        }

        public void setMentionChanged(boolean mentionChanged) {
            this.mentionChanged = mentionChanged;
        }

        public boolean isHoistedChanged() {
            return hoistedChanged;
        }

        public void setHoistedChanged(boolean hoistedChanged) {
            this.hoistedChanged = hoistedChanged;
        }

        public enum BooleanValueTyp {
            HOISTED, DELETE, CREATE, MENTION
        }
    }

}
