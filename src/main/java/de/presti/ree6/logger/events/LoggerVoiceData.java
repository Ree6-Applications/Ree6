package de.presti.ree6.logger.events;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;

/**
 * This class is used for merging Voice Activity Logs to save Webhook Messages
 * to prevent Rate-Limits.
 */
public class LoggerVoiceData {

    // An instance of the Member Entity.
    private Member member;

    // The Audio Channels associated with the Events.
    private AudioChannel previousVoiceChannel, currentVoiceChannel;

    // The Event typ.
    private LoggerVoiceTyp loggerVoiceTyp;

    /**
     * Constructor for an Audio Channel join / leave Event.
     *
     * @param member         the {@link Member} associated with the Event.
     * @param voiceChannel   the {@link AudioChannel} associated with the Event.
     * @param loggerVoiceTyp the {@link LoggerVoiceTyp} of the Event.
     */
    public LoggerVoiceData(Member member, AudioChannel voiceChannel, LoggerVoiceTyp loggerVoiceTyp) {
        this.member = member;
        if (loggerVoiceTyp == LoggerVoiceTyp.JOIN) {
            this.currentVoiceChannel = voiceChannel;
        } else if (loggerVoiceTyp == LoggerVoiceTyp.LEAVE) {
            this.previousVoiceChannel = voiceChannel;
        }
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    /**
     * Constructor for a Member Audio Channel Move Event.
     *
     * @param member               the {@link Member} associated with the Event.
     * @param previousVoiceChannel the previous {@link AudioChannel} of the Event.
     * @param currentVoiceChannel  the current {@link AudioChannel} of the Event.
     * @param loggerVoiceTyp       the {@link LoggerVoiceTyp} of the Event.
     */
    public LoggerVoiceData(Member member, AudioChannel previousVoiceChannel, AudioChannel currentVoiceChannel, LoggerVoiceTyp loggerVoiceTyp) {
        this.member = member;
        this.previousVoiceChannel = previousVoiceChannel;
        this.currentVoiceChannel = currentVoiceChannel;
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    /**
     * Get the Member that is associated with the Log.
     *
     * @return the {@link Member}.
     */
    public Member getMember() {
        return member;
    }

    /**
     * Change the associated Member of the Log.
     *
     * @param member the new {@link Member}.
     */
    public void setMember(Member member) {
        this.member = member;
    }

    /**
     * Get the previous {@link AudioChannel} of the {@link Member}
     *
     * @return the previous {@link AudioChannel}.
     */
    public AudioChannel getPreviousVoiceChannel() {
        return previousVoiceChannel;
    }

    /**
     * Change the previous {@link AudioChannel} of the {@link Member}
     *
     * @param previousVoiceChannel the new previous {@link AudioChannel}.
     */
    public void setPreviousVoiceChannel(AudioChannel previousVoiceChannel) {
        this.previousVoiceChannel = previousVoiceChannel;
    }

    /**
     * Get the current {@link AudioChannel} of the {@link Member}
     *
     * @return the current {@link AudioChannel}.
     */
    public AudioChannel getCurrentVoiceChannel() {
        return currentVoiceChannel;
    }

    /**
     * Change the current {@link AudioChannel} of the {@link Member}
     *
     * @param currentVoiceChannel the new current {@link AudioChannel}.
     */
    public void setCurrentVoiceChannel(AudioChannel currentVoiceChannel) {
        this.currentVoiceChannel = currentVoiceChannel;
    }

    /**
     * The current {@link LoggerVoiceTyp} of the Event-Log.
     * @return the {@link LoggerVoiceTyp}.
     */
    public LoggerVoiceTyp getLoggerVoiceTyp() {
        return loggerVoiceTyp;
    }

    /**
     * Change the current {@link LoggerVoiceTyp} of the Event-Log.
     * @param loggerVoiceTyp the new {@link LoggerVoiceTyp}.
     */
    public void setLoggerVoiceTyp(LoggerVoiceTyp loggerVoiceTyp) {
        this.loggerVoiceTyp = loggerVoiceTyp;
    }

    /**
     * The various LoggerVoiceTypes.
     */
    public enum LoggerVoiceTyp {
        JOIN, MOVE, LEAVE
    }
}
