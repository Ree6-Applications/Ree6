package de.presti.ree6.logger.events;

import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.ree6.logger.LogMessage;
import de.presti.ree6.logger.LogTyp;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * This is class is used to store MemberData for Logs which work with
 * Data of Members.
 */
public class LogMessageMember extends LogMessage {

    /**
     * The Member Entity.
     */
    private Member member;

    /**
     * If it is a Name change Event, two variables to store the previous and current Name.
     */
    private String previousName, currentName;

    /**
     * If it is a Role change Event, two variables to store the removed and added Roles.
     */
    private List<Role> removedRoles = new ArrayList<>(), addedRoles = new ArrayList<>();

    /**
     * Constructor for a Log-Message which shouldn't be handled.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     */
    public LogMessageMember(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp) {
        super(webhookId, webhookAuthCode, webhookMessage, guild, logTyp);
    }

    /**
     * Constructor for a Log-Message which shouldn't be handled.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     * @param member          The Member related to the Log-Message.
     */
    public LogMessageMember(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp, Member member) {
        super(webhookId, webhookAuthCode, webhookMessage, guild, logTyp);
        this.member = member;
    }

    /**
     * Constructor for a Name change Event.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     * @param member          the Guild {@link Member}.
     * @param previousName    the previous Name of the Member.
     * @param currentName     the current Name of the Member.
     */
    public LogMessageMember(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp, Member member,
                            String previousName, String currentName) {
        super(webhookId, webhookAuthCode, webhookMessage, guild, logTyp);
        this.member = member;
        this.previousName = previousName;
        this.currentName = currentName;
    }

    /**
     * Constructor for the Role change Event.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     * @param member          the Guild {@link Member}.
     * @param removedRoles    the Removed {@link Role} of the {@link Member}.
     * @param addedRoles      the Added {@link Role} of the {@link Member}.
     */
    public LogMessageMember(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp, Member member,
                            List<Role> removedRoles, List<Role> addedRoles) {
        super(webhookId, webhookAuthCode, webhookMessage, guild, logTyp);
        this.member = member;
        this.removedRoles = removedRoles;
        this.addedRoles = addedRoles;
    }

    /**
     * Constructor for everything.
     *
     * @param webhookId       The ID of the Webhook.
     * @param webhookAuthCode The Auth-Token for the Webhook.
     * @param webhookMessage  WebhookMessage itself.
     * @param guild           The Guild related to the Log-Message
     * @param logTyp          The Typ of the current Log.
     * @param member          the Guild {@link Member}.
     * @param previousName    the previous Name of the Member.
     * @param currentName     the current Name of the Member.
     * @param removedRoles    the Removed {@link Role} of the {@link Member}.
     * @param addedRoles      the Added {@link Role} of the {@link Member}.
     */
    public LogMessageMember(long webhookId, String webhookAuthCode, WebhookMessage webhookMessage, Guild guild, LogTyp logTyp,
                            Member member, String previousName, String currentName, List<Role> removedRoles, List<Role> addedRoles) {
        super(webhookId, webhookAuthCode, webhookMessage, guild, logTyp);
        this.member = member;
        this.previousName = previousName;
        this.currentName = currentName;
        this.removedRoles = removedRoles;
        this.addedRoles = addedRoles;
    }

    /**
     * Get the Member that is associated with the Log.
     *
     * @return the {@link Member}
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
     * Get the Previous Name of the {@link Member}.
     *
     * @return the previous Name as {@link String}.
     */
    public String getPreviousName() {
        return previousName;
    }

    /**
     * Change the previous Name of the {@link Member}.
     *
     * @param previousName the new previous Name as {@link String}.
     */
    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }

    /**
     * Get the current Name of the {@link Member}.
     *
     * @return the current Name as {@link String}
     */
    public String getCurrentName() {
        return currentName;
    }

    /**
     * Change the current Name of the {@link Member}.
     *
     * @param currentName the new current Name as {@link String}.
     */
    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    /**
     * Get the remove {@link Role}s of the Member.
     *
     * @return a {@link List<Role>} with every removed Role.
     */
    public List<Role> getRemovedRoles() {
        if (removedRoles == null)
            return removedRoles = new ArrayList<>();

        return removedRoles;
    }

    /**
     * Set a new deleted Roles as {@link List<Role>}.
     *
     * @param removedRoles the new {@link List<Role>}.
     */
    public void setRemovedRoles(List<Role> removedRoles) {
        getRemovedRoles().addAll(removedRoles);
    }

    /**
     * Get the add {@link Role}s of the Member.
     *
     * @return a {@link List<Role>} with every added Role.
     */
    public List<Role> getAddedRoles() {
        if (addedRoles == null)
            return addedRoles = new ArrayList<>();

        return addedRoles;
    }

    /**
     * Set a new added Roles as {@link List<Role>}.
     *
     * @param addedRoles the new {@link List<Role>}.
     */
    public void setAddedRoles(List<Role> addedRoles) {
        getAddedRoles().addAll(addedRoles);
    }
}
