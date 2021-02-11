package de.presti.ree6.invtielogger;

public class InviteContainer {

    String creatorid;
    String guildid;
    String code;
    int uses;

    public InviteContainer(String creatorid, String guildid, String code, int uses) {
        this.creatorid = creatorid;
        this.guildid = guildid;
        this.code = code;
        this.uses = uses;
    }

    public String getCreatorid() {
        return creatorid;
    }

    public void setCreatorid(String creatorid) {
        this.creatorid = creatorid;
    }

    public String getGuildid() {
        return guildid;
    }

    public void setGuildid(String guildid) {
        this.guildid = guildid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }
}
