package de.presti.ree6.logger;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;

public class LoggerVoiceData {

    private Member member;
    private AudioChannel previousVoiceChannel, currentVoiceChannel;

    private LoggerVoiceTyp loggerVoiceTyp;

    public LoggerVoiceData(Member member, AudioChannel voiceChannel, LoggerVoiceTyp loggerVoiceTyp) {
        this.member = member;
        if (loggerVoiceTyp == LoggerVoiceTyp.JOIN) {
            this.currentVoiceChannel = voiceChannel;
        } else if (loggerVoiceTyp == LoggerVoiceTyp.LEAVE) {
            this.previousVoiceChannel = voiceChannel;
        }
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    public LoggerVoiceData(Member member, AudioChannel previousVoiceChannel, AudioChannel currentVoiceChannel, LoggerVoiceTyp loggerVoiceTyp) {
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

    public AudioChannel getPreviousVoiceChannel() {
        return previousVoiceChannel;
    }

    public void setPreviousVoiceChannel(AudioChannel previousVoiceChannel) {
        this.previousVoiceChannel = previousVoiceChannel;
    }

    public AudioChannel getCurrentVoiceChannel() {
        return currentVoiceChannel;
    }

    public void setCurrentVoiceChannel(AudioChannel currentVoiceChannel) {
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
