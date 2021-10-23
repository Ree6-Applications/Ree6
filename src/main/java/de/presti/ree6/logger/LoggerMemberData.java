package de.presti.ree6.logger;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;

public class LoggerMemberData {

    private Member member;
    private String previousName, currentName;

    private ArrayList<Role> removedRoles = new ArrayList<>(), addedRoles = new ArrayList<>();

    public LoggerMemberData(Member member, String previousName, String currentName) {
        this.member = member;
        this.previousName = previousName;
        this.currentName = currentName;
    }

    public LoggerMemberData(Member member, ArrayList<Role> removedRoles, ArrayList<Role> addedRoles) {
        this.member = member;
        this.removedRoles = removedRoles;
        this.addedRoles = addedRoles;
    }

    public LoggerMemberData(Member member, String previousName, String currentName, ArrayList<Role> removedRoles, ArrayList<Role> addedRoles) {
        this.member = member;
        this.previousName = previousName;
        this.currentName = currentName;
        this.removedRoles = removedRoles;
        this.addedRoles = addedRoles;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }

    public String getCurrentName() {
        return currentName;
    }

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    public ArrayList<Role> getRemovedRoles() {
        return removedRoles;
    }

    public void setRemovedRoles(ArrayList<Role> removedRoles) {
        this.removedRoles = removedRoles;
    }

    public ArrayList<Role> getAddedRoles() {
        return addedRoles;
    }

    public void setAddedRoles(ArrayList<Role> addedRoles) {
        this.addedRoles = addedRoles;
    }
}
