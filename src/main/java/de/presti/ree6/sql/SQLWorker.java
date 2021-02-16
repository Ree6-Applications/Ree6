package de.presti.ree6.sql;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.invtielogger.InviteContainer;
import de.presti.ree6.invtielogger.InviteContainerManager;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Webhook;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class SQLWorker {

    //Leveling

    public Integer getXP(String gid, String uid) {
        String xp = "0";

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
                rs = st.executeQuery("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }

            if (rs.next()) {
                xp = rs.getString("XP");
            }

        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return Integer.parseInt(xp);
    }

    public boolean existsXP(String gid, String uid) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
                rs = st.executeQuery("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }

            if (rs.next()) {
                return true;
            }

        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return false;
    }

    public void addXP(String gid, String uid, int addxp) throws SQLException {

        addxp += getXP(gid, uid);

        if (existsXP(gid, uid)) {
            Main.sqlConnector.query("UPDATE Level SET XP='" + addxp + "' WHERE GID='" + gid + "' AND UID='" + uid + "'");
        } else {
            Main.sqlConnector.query("INSERT INTO Level (GID, UID, XP) VALUES ('" + gid + "', '" + uid + "', '" + addxp + "');");
        }
    }

    public ArrayList<String> getTop(int amount, String gid) {

        ArrayList<String> ids = new ArrayList<>();

        try {

            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM `Level` WHERE GID='" + gid + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + amount);
                rs = st.executeQuery("SELECT * FROM `Level` WHERE GID='" + gid + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + amount);
            } catch (Exception ex) {}

            while (rs.next()) {
                ids.add(rs.getString("UID"));
            }

        } catch (Exception ex) {}

        return ids;
    }

    //Logging

    public void setLogWebhook(String gid, String cid, String token) throws SQLException {
        if (hasLogSetuped(gid)) {

            String[] d = getLogwebhook(gid);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });

            Main.insance.sqlConnector.query("DELETE FROM LogWebhooks WHERE GID='" + gid + "'");
            Main.insance.sqlConnector.query("INSERT INTO LogWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
        } else {
            Main.insance.sqlConnector.query("INSERT INTO LogWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
        }
    }

    public boolean hasLogSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
            } catch (Exception ex) {
            }

            if (rs.next()) {
                return true;
            }

        } catch (Exception ex) {
        }
        return false;
    }

    public String[] getLogwebhook(String gid) {
        if (hasLogSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new String[]{"Error", "Not setuped!"};
    }


    //Welcome

    public void setWelcomeWebhook(String gid, String cid, String token) throws SQLException {
        if (hasWeclomeSetuped(gid)) {

            String[] d = getWelcomewebhook(gid);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });
            Main.insance.sqlConnector.query("DELETE FROM WelcomeWebhooks WHERE GID='" + gid + "'");
            Main.insance.sqlConnector.query("INSERT INTO WelcomeWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
        } else {
            Main.insance.sqlConnector.query("INSERT INTO WelcomeWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
        }
    }

    public boolean hasWeclomeSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
            } catch (Exception ex) {
            }

            if (rs.next()) {
                return true;
            }

        } catch (Exception ex) {
        }
        return false;
    }

    public String[] getWelcomewebhook(String gid) {
        if (hasWeclomeSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new String[]{"Error", "Not setuped!"};
    }

    //Mute

    public void setMuteRole(String gid, String rid) throws SQLException {
        if (hasMuteSetuped(gid)) {
            Main.insance.sqlConnector.query("UPDATE MuteRoles SET RID='" + rid + "' WHERE GID='" + gid + "'");
        } else {
            Main.insance.sqlConnector.query("INSERT INTO MuteRoles (GID, RID) VALUES ('" + gid + "', '" + rid + "');");
        }
    }

    public boolean hasMuteSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM MuteRoles WHERE GID='" + gid + "'");
            } catch (Exception ex) {
            }

            if (rs.next()) {
                return true;
            }

        } catch (Exception ex) {
        }
        return false;
    }

    public String getMuteRoleID(String gid) {
        if (hasMuteSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM MuteRoles WHERE GID='" + gid + "'");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (rs.next()) {
                    return rs.getString("RID");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "Error";
    }

    //Autorole

    public boolean hasAutoRoles(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM AutoRoles WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            return rs.next();

        } catch (Exception ex) {
        }

        return false;
    }

    public ArrayList<String> getAutoRoleIDs(String gid) {

        ArrayList<String> roles = new ArrayList<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM AutoRoles WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            while (rs.next()) {
                roles.add(rs.getString("RID"));
            }

        } catch (Exception ex) {
        }

        return roles;
    }

    public void addAutoRole(String gid, String rid) {
        Main.insance.sqlConnector.query("INSERT INTO AutoRoles (GID, RID) VALUES ('" + gid + "', '" + rid + "');");
    }

    public void removeAutoRole(String gid, String rid) {
        Main.insance.sqlConnector.query("DELETE FROM AutoRoles WHERE GID='" + gid + "' AND RID='" + rid + "'");
    }

    //Invite

    public boolean existsInvite(String gid, String code, String creator) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Invites WHERE GID='" + gid + "' AND UID='" + creator + "' AND CODE='" + code + "'");
                rs = st.executeQuery("SELECT * FROM Invites WHERE GID='" + gid + "' AND UID='" + creator + "' AND CODE='" + code + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            if(rs.next()) {
                return true;
            }

        } catch (Exception ex) {
        }

        return false;
    }

    public ArrayList<InviteContainer> getInvites(String gid) {

        ArrayList<InviteContainer> pog = new ArrayList<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Invites WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM Invites WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            while(rs.next()) {
                pog.add(new InviteContainer(rs.getString("UID"), rs.getString("GID"), rs.getString("CODE"), Integer.parseInt(rs.getString("USES"))));
            }

        } catch (Exception ex) {
        }

        return pog;
    }

    public void setInvite(String gid, String code, String creator, int usage) throws SQLException {
        if (existsInvite(gid, code, creator)) {
            Main.insance.sqlConnector.query("UPDATE Invites SET USES='" + usage + "' WHERE GID='" + gid + "' AND UID='" + creator + "' AND CODE='" + code + "'");
        } else {
            Main.insance.sqlConnector.query("INSERT INTO Invites (GID, UID, USES, CODE) VALUES ('" + gid + "', '" + creator + "', '" + usage + "', '" + code + "');");
        }
    }

    public void removeInvite(String gid, String creator, String code) {
        Main.insance.sqlConnector.query("DELETE FROM Invites WHERE GID='" + gid + "' AND UID='" + creator + "' AND CODE='" + code + "'");
    }

    public void saveAllInvites() throws SQLException {
        for(Map.Entry<String, ArrayList<InviteContainer>> entry : InviteContainerManager.getInvites().entrySet()) {
            for(InviteContainer inv : entry.getValue()) {
                setInvite(entry.getKey(), inv.getCode(), inv.getCreatorid(), inv.getUses());
            }
        }

        for(InviteContainer inv : InviteContainerManager.getDeletedInvites()) {
            removeInvite(inv.getGuildid(), inv.getCreatorid(), inv.getCode());
        }
    }

    public void loadAllInvites() {
        for(Guild g : BotInfo.botInstance.getGuilds()) {
            ArrayList<InviteContainer> invs = getInvites(g.getId());

            if(!invs.isEmpty()) {
                if (InviteContainerManager.getInvites().containsKey(g.getId())) {
                    InviteContainerManager.getInvites().remove(g.getId());
                }

                InviteContainerManager.getInvites().put(g.getId(), invs);
            }
        }
    }

    public void deleteAllMyData(String gid) {
        Main.insance.sqlConnector.query("DELETE FROM Invites WHERE GID='" + gid + "'");
        Main.insance.sqlConnector.query("DELETE FROM AutoRoles WHERE GID='" + gid + "'");
        Main.insance.sqlConnector.query("DELETE FROM WelcomeWebhooks WHERE GID='" + gid + "'");
        Main.insance.sqlConnector.query("DELETE FROM LogWebhooks WHERE GID='" + gid + "'");
    }
}