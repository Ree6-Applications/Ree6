package de.presti.ree6.logger;

/**
 * The used Log-Types.
 */
public enum LogTyp {
    /**
     * LogTyp for the Logs which are fired when a User joins a Guild.
     */
    SERVER_JOIN,
    /**
     * LogTyp for the Logs which are fired when a User leaves a Guild.
     */
    SERVER_LEAVE,
    /**
     * LogTyp for the Logs which are fired when a User gets invited to a Guild
     */
    SERVER_INVITE,
    /**
     * LogTyp for the Logs which are fired when a User joins a Voice-channel.
     */
    VC_JOIN,
    /**
     * LogTyp for the Logs which are fired when a User moves between Voice channels.
     */
    VC_MOVE,
    /**
     * LogTyp for the Logs which are fired when a User leaves a Voice-channel.
     */
    VC_LEAVE,
    /**
     * LogTyp for the Logs which are fired when a User changes their Nickname.
     */
    NICKNAME_CHANGE,
    /**
     * LogTyp for the Logs which are fired when a Role gets changed.
     */
    ROLEDATA_CHANGE,
    /**
     * LogTyp for the Logs which are fired when User role related data changes.
     */
    MEMBERROLE_CHANGE,
    /**
     * LogTyp for the Logs which are fired when channel data gets changed.
     */
    CHANNELDATA_CHANGE,
    /**
     * LogTyp for the Logs which are fired when a User gets banned.
     */
    USER_BAN,
    /**
     * LogTyp for the Logs which are fired when a User gets unbanned.
     */
    USER_UNBAN,
    /**
     * LogTyp for the Logs which are fired when a Message gets deleted.
     */
    MESSAGE_DELETE,
    /**
     * LogTyp for not yet implemented Logs.
     */
    ELSE
}