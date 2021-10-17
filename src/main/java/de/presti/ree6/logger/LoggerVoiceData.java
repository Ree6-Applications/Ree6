package de.presti.ree6.logger;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class LoggerVoiceData {

    private Member member;
    private VoiceChannel previousVoiceChannel, currentVoiceChannel;

    private LoggerVoiceTyp loggerVoiceTyp;

    public LoggerVoiceData(Member member, VoiceChannel voiceChannel, LoggerVoiceTyp loggerVoiceTyp) {
        this.member = member;
        if (loggerVoiceTyp == LoggerVoiceTyp.JOIN) {
            this.currentVoiceChannel = voiceChannel;
        } else if (loggerVoiceTyp == LoggerVoiceTyp.LEAVE) {
            this.previousVoiceChannel = voiceChannel;
        }
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    public LoggerVoiceData(Member member, VoiceChannel previousVoiceChannel, VoiceChannel currentVoiceChannel, LoggerVoiceTyp loggerVoiceTyp) {
        this.member = member;
        this.previousVoiceChannel = previousVoiceChannel;
        this.currentVoiceChannel = currentVoiceChannel;
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public VoiceChannel getPreviousVoiceChannel() {
        return previousVoiceChannel;
    }

    public void setPreviousVoiceChannel(VoiceChannel previousVoiceChannel) {
        this.previousVoiceChannel = previousVoiceChannel;
    }

    public VoiceChannel getCurrentVoiceChannel() {
        return currentVoiceChannel;
    }

    public void setCurrentVoiceChannel(VoiceChannel currentVoiceChannel) {
        this.currentVoiceChannel = currentVoiceChannel;
    }

    public LoggerVoiceTyp getLoggerVoiceTyp() {
        return loggerVoiceTyp;
    }

    public void setLoggerVoiceTyp(LoggerVoiceTyp loggerVoiceTyp) {
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    public enum LoggerVoiceTyp {
        JOIN, MOVE, LEAVE
    }
}
