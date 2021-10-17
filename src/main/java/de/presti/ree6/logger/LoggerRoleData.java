package de.presti.ree6.logger;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.EnumSet;

public class LoggerRoleData {

    long roleId;

    String previousName, currentName;
    Color previousColor, currentColor;
    EnumSet<Permission> previousPermission, currentPermission;

    boolean IsCreated, IsDeleted, IsHoisted, IsMentioned, changedMentioned, changedHoisted;

    public LoggerRoleData(Role role) {
        this.roleId = role.getIdLong();
        this.currentName = role.getName();
        this.currentColor = role.getColor();
        this.currentPermission = role.getPermissions();
        setChangedHoisted(role.isHoisted());
        setChangedMentioned(role.isMentionable());
    }

    public LoggerRoleData(long roleId, String previousName, String currentName) {
        this.roleId = roleId;
        this.previousName = previousName;
        this.currentName = currentName;
    }

    public LoggerRoleData(long roleId, String previousName, String currentName, boolean isCreated) {
        this.roleId = roleId;
        this.previousName = previousName;
        this.currentName = currentName;
        IsCreated = isCreated;
    }

    public LoggerRoleData(long roleId, String currentName, boolean isCreated) {
        this.roleId = roleId;
        this.currentName = currentName;
        IsCreated = isCreated;
        IsDeleted = !isCreated;
    }

    public LoggerRoleData(long roleId, Color previousColor, Color currentColor) {
        this.roleId = roleId;
        this.previousColor = previousColor;
        this.currentColor = currentColor;
    }

    public LoggerRoleData(long roleId, Color previousColor, Color currentColor, boolean isCreated) {
        this.roleId = roleId;
        this.previousColor = previousColor;
        this.currentColor = currentColor;
        IsCreated = isCreated;
    }

    public LoggerRoleData(long roleId, EnumSet<Permission> previousPermission, EnumSet<Permission> currentPermission) {
        this.roleId = roleId;
        this.previousPermission = previousPermission;
        this.currentPermission = currentPermission;
    }

    public LoggerRoleData(long roleId, EnumSet<Permission> previousPermission, EnumSet<Permission> currentPermission, boolean isCreated) {
        this.roleId = roleId;
        this.previousPermission = previousPermission;
        this.currentPermission = currentPermission;
        IsCreated = isCreated;
    }

    public LoggerRoleData(long roleId, String currentName, boolean isCreated, boolean isDeleted, boolean isHoisted, boolean isMentioned) {
        this.roleId = roleId;
        this.currentName = currentName;
        IsCreated = isCreated;
        IsDeleted = isDeleted;
        setChangedHoisted(isHoisted);
        setChangedMentioned(isMentioned);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
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

    public Color getPreviousColor() {
        return previousColor;
    }

    public void setPreviousColor(Color previousColor) {
        this.previousColor = previousColor;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public EnumSet<Permission> getPreviousPermission() {
        return previousPermission;
    }

    public void setPreviousPermission(EnumSet<Permission> previousPermission) {
        this.previousPermission = previousPermission;
    }

    public EnumSet<Permission> getCurrentPermission() {
        return currentPermission;
    }

    public void setCurrentPermission(EnumSet<Permission> currentPermission) {
        this.currentPermission = currentPermission;
    }

    public boolean isCreated() {
        return IsCreated;
    }

    public void setCreated(boolean created) {
        IsCreated = created;
    }

    public boolean isDeleted() {
        return IsDeleted;
    }

    public void setDeleted(boolean deleted) {
        IsDeleted = deleted;
    }

    public boolean isHoisted() {
        return IsHoisted;
    }

    public void setHoisted(boolean hoisted) {
        IsHoisted = hoisted;
        changedHoisted = true;
    }

    public boolean isMentioned() {
        return IsMentioned;
    }

    public void setMentioned(boolean mentioned) {
        IsMentioned = mentioned;
        changedMentioned = true;
    }

    public boolean isChangedMentioned() {
        return changedMentioned;
    }

    public void setChangedMentioned(boolean changedMentioned) {
        this.changedMentioned = changedMentioned;
    }

    public boolean isChangedHoisted() {
        return changedHoisted;
    }

    public void setChangedHoisted(boolean changedHoisted) {
        this.changedHoisted = changedHoisted;
    }
}
