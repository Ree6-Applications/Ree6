package de.presti.ree6.logger;

import club.minnced.discord.webhook.send.WebhookMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class LoggerMessage {

    private long id = 0L;
    private String authcode = "";
    private boolean cancel = false;

    private WebhookMessage wem;

    private Member m;
    private VoiceChannel vc;
    private VoiceChannel vc2;

    private LogTyp type;

    public LoggerMessage(long c, String auth, WebhookMessage wem, Member m, LogTyp type) {
        this.id = c;
        this.authcode = auth;
        this.wem = wem;
        this.m = m;
        this.type = type;
    }

    public LoggerMessage(long c, String auth, WebhookMessage wem, Member m, LogTyp type, VoiceChannel vc) {
        this.id = c;
        this.authcode = auth;
        this.wem = wem;
        this.m = m;
        this.type = type;
        this.vc = vc;
    }

    public LoggerMessage(long c, String auth, WebhookMessage wem, Member m, LogTyp type, VoiceChannel vc, VoiceChannel vc2) {
        this.id = c;
        this.authcode = auth;
        this.wem = wem;
        this.m = m;
        this.type = type;
        this.vc = vc;
        this.vc2 = vc2;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthcode() {
        return authcode;
    }

    public void setAuthcode(String authcode) {
        this.authcode = authcode;
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

    public enum LogTyp {
        VC_LEAVE, VC_JOIN, VC_MOVE, ELSE
    }
}
