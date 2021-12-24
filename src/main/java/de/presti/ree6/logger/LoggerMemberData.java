package de.presti.ree6.logger;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;

/**
 * This is class is used to store MemberData for Logs which work with
 * Data of Members.
 */
public class LoggerMemberData {

    // An instance of the Member Entity.
    private Member member;

    // If it is a Name change Event, two variables to store the previous and current Name.
    private String previousName, currentName;

    // If it is a Role change Event, two variables to store the removed and added Roles.
    private ArrayList<Role> removedRoles = new ArrayList<>(), addedRoles = new ArrayList<>();

    /**
     * Constructor for a Name change Event.
     *
     * @param member       the Guild {@link Member}.
     * @param previousName the previous Name of the Member.
     * @param currentName  the current Name of the Member.
     */
    public LoggerMemberData(Member member, String previousName, String currentName) {
        this.member = member;
        this.previousName = previousName;
        this.currentName = currentName;
    }

    /**
     * Constructor for the Role change Event.
     *
     * @param member       the Guild {@link Member}.
     * @param removedRoles the Removed {@link Role} of the {@link Member}.
     * @param addedRoles   the Added {@link Role} of the {@link Member}.
     */
    public LoggerMemberData(Member member, ArrayList<Role> removedRoles, ArrayList<Role> addedRoles) {
        this.member = member;
        this.removedRoles = removedRoles;
        this.addedRoles = addedRoles;
    }

    /**
     * Constructor for everything.
     *
     * @param member       the Guild {@link Member}.
     * @param previousName the previous Name of the Member.
     * @param currentName  the current Name of the Member.
     * @param removedRoles the Removed {@link Role} of the {@link Member}.
     * @param addedRoles   the Added {@link Role} of the {@link Member}.
     */
    public LoggerMemberData(Member member, String previousName, String currentName, ArrayList<Role> removedRoles, ArrayList<Role> addedRoles) {
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
     * @return a {@link ArrayList<Role>} with every removed Role.
     */
    public ArrayList<Role> getRemovedRoles() {
        return removedRoles;
    }

    /**
     * Set a new deleted Roles as {@link ArrayList<Role>}.
     *
     * @param removedRoles the new {@link ArrayList<Role>}.
     */
    public void setRemovedRoles(ArrayList<Role> removedRoles) {
        this.removedRoles = removedRoles;
    }

    /**
     * Get the add {@link Role}s of the Member.
     *
     * @return a {@link ArrayList<Role>} with every added Role.
     */
    public ArrayList<Role> getAddedRoles() {
        return addedRoles;
    }

    /**
     * Set a new added Roles as {@link ArrayList<Role>}.
     *
     * @param addedRoles the new {@link ArrayList<Role>}.
     */
    public void setAddedRoles(ArrayList<Role> addedRoles) {
        this.addedRoles = addedRoles;
    }
}
