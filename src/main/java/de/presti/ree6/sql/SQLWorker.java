package de.presti.ree6.sql;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.invitelogger.InviteContainer;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Webhook;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLWorker {

    //Leveling Chat

    public Long getXP(String gid, String uid) {
        String xp = "0";

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
                rs = st.executeQuery("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
            } catch (Exception ignore) {
                //ex.printStackTrace();
            }

            if (rs != null && rs.next()) {
                xp = rs.getString("XP");
            }

        } catch (Exception ignore) {
            //ex.printStackTrace();
        }

        return Long.parseLong(xp);
    }

    public boolean existsXP(String gid, String uid) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
                rs = st.executeQuery("SELECT * FROM Level WHERE GID='" + gid + "' AND UID='" + uid + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }

        return false;
    }

    public void addXP(String gid, String uid, int addxp) {

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
            } catch (Exception ignore) {
            }

            while (rs != null && rs.next()) {
                ids.add(rs.getString("UID"));
            }

        } catch (Exception ignore) {
        }

        return ids;
    }

    //Leveling VoiceChannel
    public Long getXPVC(String gid, String uid) {
        String xp = "0";

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM VCLevel WHERE GID='" + gid + "' AND UID='" + uid + "'");
                rs = st.executeQuery("SELECT * FROM VCLevel WHERE GID='" + gid + "' AND UID='" + uid + "'");
            } catch (Exception ignore) {
                //ex.printStackTrace();
            }

            if (rs != null && rs.next()) {
                xp = rs.getString("XP");
            }

        } catch (Exception ignore) {
            //ex.printStackTrace();
        }

        return Long.parseLong(xp);
    }

    public boolean existsXPVC(String gid, String uid) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM VCLevel WHERE GID='" + gid + "' AND UID='" + uid + "'");
                rs = st.executeQuery("SELECT * FROM VCLevel WHERE GID='" + gid + "' AND UID='" + uid + "'");
            } catch (Exception ignore) {
                //ex.printStackTrace();
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
            //ex.printStackTrace();
        }

        return false;
    }

    public void addXPVC(String gid, String uid, int addXP) {

        addXP += getXPVC(gid, uid);

        if (existsXPVC(gid, uid)) {
            Main.sqlConnector.query("UPDATE VCLevel SET XP='" + addXP + "' WHERE GID='" + gid + "' AND UID='" + uid + "'");
        } else {
            Main.sqlConnector.query("INSERT INTO VCLevel (GID, UID, XP) VALUES ('" + gid + "', '" + uid + "', '" + addXP + "');");
        }
    }

    public ArrayList<String> getTopVC(int amount, String gid) {

        ArrayList<String> ids = new ArrayList<>();

        try {

            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM `VCLevel` WHERE GID='" + gid + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + amount);
                rs = st.executeQuery("SELECT * FROM `VCLevel` WHERE GID='" + gid + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + amount);
            } catch (Exception ignore) {
            }

            while (rs != null && rs.next()) {
                ids.add(rs.getString("UID"));
            }

        } catch (Exception ignore) {
        }

        return ids;
    }

    //Logging

    public void setLogWebhook(String gid, String cid, String token) {
        if (hasLogSetuped(gid)) {

            String[] d = getLogWebhook(gid);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });

            Main.sqlConnector.query("DELETE FROM LogWebhooks WHERE GID='" + gid + "'");
        }
        Main.sqlConnector.query("INSERT INTO LogWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
    }

    public boolean hasLogSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }

    public String[] getLogWebhook(String gid) {
        if (hasLogSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM LogWebhooks WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ignore) {
            }
        }
        return new String[]{ "0", "Not setuped!" };
    }


    //Welcome

    public void setWelcomeWebhook(String gid, String cid, String token) {
        if (hasWelcomeSetuped(gid)) {

            String[] d = getWelcomeWebhook(gid);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });
            Main.sqlConnector.query("DELETE FROM WelcomeWebhooks WHERE GID='" + gid + "'");
        }
        Main.sqlConnector.query("INSERT INTO WelcomeWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
    }

    public boolean hasWelcomeSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            if (rs != null && rs.next()) {
                return true;
            }

        } catch (Exception ignore) {
        }
        return false;
    }

    public String[] getWelcomeWebhook(String gid) {
        if (hasWelcomeSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM WelcomeWebhooks WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ignore) {
            }
        }
        return new String[]{ "0", "Not setuped!" };
    }

    //Mute

    public void setMuteRole(String gid, String rid) {
        if (hasMuteSetuped(gid)) {
            Main.sqlConnector.query("UPDATE MuteRoles SET RID='" + rid + "' WHERE GID='" + gid + "'");
        } else {
            Main.sqlConnector.query("INSERT INTO MuteRoles (GID, RID) VALUES ('" + gid + "', '" + rid + "');");
        }
    }

    public boolean hasMuteSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM MuteRoles WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            if (rs != null && rs.next()) {
                return true;
            }

        } catch (Exception ignore) {
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
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return rs.getString("RID");
                }

            } catch (Exception ignore) {
            }
        }
        return "Error";
    }

    //ChatLevelReward

    public boolean hasChatLevelReward(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            return (rs != null && rs.next());

        } catch (Exception ignore) {
        }

        return false;
    }

    public HashMap<Integer, String> getChatLevelRewards(String gid) {

        HashMap<Integer, String> rewards = new HashMap<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            while (rs != null && rs.next()) {
                if (!rewards.containsKey(Integer.valueOf(rs.getString("LVL")))) {
                    rewards.put(Integer.parseInt(rs.getString("LVL")), rs.getString("RID"));
                }
            }

        } catch (Exception ignore) {
        }

        return rewards;
    }

    public void addChatLevelReward(String gid, int level, String rid) {
        Main.sqlConnector.query("INSERT INTO ChatLevelAutoRoles (GID, RID, LVL) VALUES ('" + gid + "', '" + rid + "','" + level + "');");
    }

    public void removeChatLevelReward(String gid, int level) {
        Main.sqlConnector.query("DELETE FROM ChatLevelAutoRoles WHERE GID='" + gid + "' AND LVL='" + level + "'");
    }


    //VoiceLevelReward

    public boolean hasVoiceLevelReward(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM VCLevelAutoRoles WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            return (rs != null && rs.next());

        } catch (Exception ignore) {
        }

        return false;
    }

    public HashMap<Integer, String> getVoiceLevelRewards(String gid) {

        HashMap<Integer, String> rewards = new HashMap<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM VCLevelAutoRoles WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            while (rs != null && rs.next()) {
                if (!rewards.containsKey(Integer.valueOf(rs.getString("LVL")))) {
                    rewards.put(Integer.parseInt(rs.getString("LVL")), rs.getString("RID"));
                }
            }

        } catch (Exception ignore) {
        }

        return rewards;
    }

    public void addVoiceLevelReward(String gid, int level, String rid) {
        Main.sqlConnector.query("INSERT INTO VCLevelAutoRoles (GID, RID, LVL) VALUES ('" + gid + "', '" + rid + "','" + level + "');");
    }

    public void removeVoiceLevelReward(String gid, int level) {
        Main.sqlConnector.query("DELETE FROM VCLevelAutoRoles WHERE GID='" + gid + "' AND LVL='" + level + "'");
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

            return (rs != null && rs.next());

        } catch (Exception ignore) {
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

            while (rs != null && rs.next()) {
                roles.add(rs.getString("RID"));
            }

        } catch (Exception ignore) {
        }

        return roles;
    }

    public void addAutoRole(String gid, String rid) {
        Main.sqlConnector.query("INSERT INTO AutoRoles (GID, RID) VALUES ('" + gid + "', '" + rid + "');");
    }

    public void removeAutoRole(String gid, String rid) {
        Main.sqlConnector.query("DELETE FROM AutoRoles WHERE GID='" + gid + "' AND RID='" + rid + "'");
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

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }

        return false;
    }

    public ArrayList<InviteContainer> getInvites(String gid) {

        ArrayList<InviteContainer> invites = new ArrayList<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Invites WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM Invites WHERE GID='" + gid + "'");
            } catch (Exception x) {
                x.printStackTrace();
            }

            while (rs != null && rs.next()) {
                invites.add(new InviteContainer(rs.getString("UID"), rs.getString("GID"), rs.getString("CODE"), Integer.parseInt(rs.getString("USES"))));
            }

        } catch (Exception ignore) {
        }

        return invites;
    }

    public void setInvite(String gid, String code, String creator, int usage) {
        if (existsInvite(gid, code, creator)) {
            Main.sqlConnector.query("UPDATE Invites SET USES='" + usage + "' WHERE GID='" + gid + "' AND UID='" + creator + "' AND CODE='" + code + "'");
        } else {
            Main.sqlConnector.query("INSERT INTO Invites (GID, UID, USES, CODE) VALUES ('" + gid + "', '" + creator + "', '" + usage + "', '" + code + "');");
        }
    }

    public void removeInvite(String gid, String creator, String code) {
        Main.sqlConnector.query("DELETE FROM Invites WHERE GID='" + gid + "' AND UID='" + creator + "' AND CODE='" + code + "'");
    }

    public void removeInvite(String gid, String code) {
        Main.sqlConnector.query("DELETE FROM Invites WHERE GID='" + gid + "' AND CODE='" + code + "'");
    }

    public void deleteAllMyData(String gid) {
        Main.sqlConnector.query("DELETE FROM Invites WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM AutoRoles WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM WelcomeWebhooks WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM LogWebhooks WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM NewsWebhooks WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM JoinMessage WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM MuteRoles WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM ChatProtector WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM TwitchNotify WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM Webinterface WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM RainbowWebhooks WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM Level WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM VCLevel WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM VCLevelAutoRoles WHERE GID='" + gid + "'");
        Main.sqlConnector.query("DELETE FROM ChatLevelAutoRoles WHERE GID='" + gid + "'");
    }

    //News

    public String[] getNewsWebhook(String gid) {
        if (hasNewsSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM NewsWebhooks WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ignore) {
            }
        }
        return new String[]{ "0", "Not setuped!" };
    }

    public void setNewsWebhook(String gid, String cid, String token) {
        if (hasNewsSetuped(gid)) {
            String[] d = getNewsWebhook(gid);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });
            Main.sqlConnector.query("DELETE FROM NewsWebhooks WHERE GID='" + gid + "'");
        }
        Main.sqlConnector.query("INSERT INTO NewsWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
    }

    public boolean hasNewsSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;
            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM NewsWebhooks WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }

    //Config

    public void setMessage(String gid, String text) {
        if (hasMessageSetuped(gid)) {
            Main.sqlConnector.query("DELETE FROM JoinMessage WHERE GID='" + gid + "'");
        }
        Main.sqlConnector.query("INSERT INTO JoinMessage (GID, MSG) VALUES ('" + gid + "', '" + text + "');");
    }

    public String getMessage(String gid) {
        if (hasMessageSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM JoinMessage WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM JoinMessage WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return rs.getString("MSG");
                }

            } catch (Exception ignore) {
            }
        }
        return "Welcome %user_mention%!\nWe wish you a great time on %guild_name%";
    }

    public boolean hasMessageSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM JoinMessage WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM JoinMessage WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }

    //ChatProtector

    public boolean hasChatProtectorSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM ChatProtector WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM ChatProtector WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }

    public boolean hasChatProtectorWord(String gid, String word) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM ChatProtector WHERE GID='" + gid + "'AND WORD='" + word + "'");
                rs = st.executeQuery("SELECT * FROM ChatProtector WHERE GID='" + gid + "' AND WORD='" + word + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }


    public void addChatProtector(String gid, String word) {
        if (hasChatProtectorWord(gid, word)) {
            Main.sqlConnector.query("DELETE FROM ChatProtector WHERE GID='" + gid + "' AND WORD='" + word + "'");
        }
        Main.sqlConnector.query("INSERT INTO ChatProtector (GID, WORD) VALUES ('" + gid + "', '" + word + "');");
    }

    public void removeChatProtector(String gid, String word) {
        if (hasChatProtectorWord(gid, word)) {
            Main.sqlConnector.query("DELETE FROM ChatProtector WHERE GID='" + gid + "' AND WORD='" + word + "'");
        }
    }

    public ArrayList<String> getChatProtector(String gid) {
        ArrayList<String> chatProtection = new ArrayList<>();
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM ChatProtector WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM ChatProtector WHERE GID='" + gid + "'");
            } catch (Exception ignore) {
            }

            while (rs != null && rs.next()) {
                chatProtection.add(rs.getString("WORD"));
            }

        } catch (Exception ignore) {
        }

        return chatProtection;
    }

    //Rainbow

    public String[] getRainbowHooks(String gid) {
        if (hasRainbowSetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM RainbowWebhooks WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM RainbowWebhooks WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ignore) {
            }
        }
        return new String[]{ "0", "Not setuped!" };
    }

    public void setRainbowWebhook(String gid, String cid, String token) {
        if (hasRainbowSetuped(gid)) {
            String[] d = getRainbowHooks(gid);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });
            Main.sqlConnector.query("DELETE FROM RainbowWebhooks WHERE GID='" + gid + "'");
        }
        Main.sqlConnector.query("INSERT INTO RainbowWebhooks (GID, CID, TOKEN) VALUES ('" + gid + "', '" + cid + "', '" + token + "');");
    }

    public boolean hasRainbowSetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM RainbowWebhooks WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM RainbowWebhooks WHERE GID='" + gid + "'");
            } catch (Exception ignored) {
            }

            return rs != null && rs.next();

        } catch (Exception ignored) {
        }
        return false;
    }

    //Twitch

    public ArrayList<String[]> getTwitchNotifyWebhooks(String gid) {
        ArrayList<String[]> webhooks = new ArrayList<>();
        if (hasTwitchNotifySetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM TwitchNotify WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                while (rs != null && rs.next()) {
                    webhooks.add(new String[]{rs.getString("CID"), rs.getString("TOKEN")});
                }

            } catch (Exception ignore) {
            }
        }
        return webhooks;
    }

    public ArrayList<String> getTwitchNotifier(String gid) {
        ArrayList<String> names = new ArrayList<>();
        if (hasTwitchNotifySetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM TwitchNotify WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                while (rs != null && rs.next()) {
                    names.add(rs.getString("NAME"));
                }

            } catch (Exception ignore) {
            }
        }
        return names;
    }

    public ArrayList<String> getAllTwitchNotifyUsers() {
        ArrayList<String> names = new ArrayList<>();
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify");
                rs = st.executeQuery("SELECT * FROM TwitchNotify");
            } catch (Exception ignore) {
            }

            while (rs != null && rs.next()) {
                names.add(rs.getString("NAME"));
            }

        } catch (Exception ignore) {
        }
        return names;
    }

    public String[] getTwitchNotifyWebhook(String gid, String name) {
        if (hasTwitchNotifySetuped(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + gid + "' AND NAME='" + name + "'");
                    rs = st.executeQuery("SELECT * FROM TwitchNotify WHERE GID='" + gid + "' AND NAME='" + name + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }

            } catch (Exception ignore) {
            }
        }
        return new String[]{ "0", "Not setuped!" };
    }

    public String[] getTwitchNotifyWebhookByName(String name) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify WHERE NAME='" + name + "'");
                rs = st.executeQuery("SELECT * FROM TwitchNotify WHERE NAME='" + name + "'");
            } catch (Exception ignore) {
            }

            if (rs != null && rs.next()) {
                return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
            }

        } catch (Exception ignore) {
        }
        return new String[]{ "0", "Not setuped!" };
    }


    public void addTwitchNotify(String gid, String name, String cid, String token) {
        if (hasTwitchNotifySetupedForUser(gid, name)) {
            String[] d = getTwitchNotifyWebhook(gid, name);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });
            Main.sqlConnector.query("DELETE FROM TwitchNotify WHERE GID='" + gid + "' AND NAME='" + name + "'");
        }
        Main.sqlConnector.query("INSERT INTO TwitchNotify (GID, NAME, CID, TOKEN) VALUES ('" + gid + "', '" + name + "','" + cid + "', '" + token + "');");
    }

    public void removeTwitchNotify(String gid, String name) {
        if (hasTwitchNotifySetupedForUser(gid, name)) {
            String[] d = getTwitchNotifyWebhook(gid, name);

            BotInfo.botInstance.getGuildById(gid).retrieveWebhooks().queue(webhooks -> {
                for (Webhook wb : webhooks) {
                    if (wb.getId().equalsIgnoreCase(d[0]) && wb.getToken().equalsIgnoreCase(d[1])) {
                        wb.delete().queue();
                    }
                }
            });
            Main.sqlConnector.query("DELETE FROM TwitchNotify WHERE GID='" + gid + "' AND NAME='" + name + "'");
        }
    }

    public boolean hasTwitchNotifySetuped(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM TwitchNotify WHERE GID='" + gid + "'");
            } catch (Exception ignored) {
            }

            return rs != null && rs.next();

        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean hasTwitchNotifySetupedForUser(String gid, String name) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + gid + "' AND NAME='" + name + "'");
                rs = st.executeQuery("SELECT * FROM TwitchNotify WHERE GID='" + gid + "' AND NAME='" + name + "'");
            } catch (Exception ignored) {
            }

            return rs != null && rs.next();

        } catch (Exception ignored) {
        }
        return false;
    }

    //Webinterface Auth

    public String getAuthToken(String gid) {
        if (hasAuthToken(gid)) {
            try {
                PreparedStatement st;
                ResultSet rs = null;

                try {
                    st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Webinterface WHERE GID='" + gid + "'");
                    rs = st.executeQuery("SELECT * FROM Webinterface WHERE GID='" + gid + "'");
                } catch (Exception ignore) {
                }

                if (rs != null && rs.next()) {
                    return rs.getString("AUTH");
                }

            } catch (Exception ignore) {
            }
        }
        return "0";
    }

    public void setAuthToken(String gid, String auth) {
        if (hasAuthToken(gid)) {
            Main.sqlConnector.query("DELETE FROM Webinterface WHERE GID='" + gid + "'");
        }
        Main.sqlConnector.query("INSERT INTO Webinterface (GID, AUTH) VALUES ('" + gid + "', '" + auth + "');");
    }

    public void deleteAuthToken(String gid) {
        if (hasAuthToken(gid)) {
            Main.sqlConnector.query("DELETE FROM Webinterface WHERE GID='" + gid + "'");
        }
    }

    public boolean hasAuthToken(String gid) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Webinterface WHERE GID='" + gid + "'");
                rs = st.executeQuery("SELECT * FROM Webinterface WHERE GID='" + gid + "'");
            } catch (Exception ignored) {
            }

            return rs != null && rs.next();

        } catch (Exception ignored) {
        }
        return false;
    }

    //Stats

    public boolean hasStatsGuild(String gid, String command) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM GuildStats WHERE GID='" + gid + "' AND COMMAND='" + command + "'");
                rs = st.executeQuery("SELECT * FROM GuildStats WHERE GID='" + gid + "' AND COMMAND='" + command + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }

    public boolean hasStatsCommand(String command) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM CommandStats WHERE COMMAND='" + command + "'");
                rs = st.executeQuery("SELECT * FROM CommandStats WHERE COMMAND='" + command + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }
        return false;
    }

    public HashMap<String, Long> getStatsFromGuild(String gid) {

        HashMap<String, Long> data = new HashMap<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM GuildStats WHERE GID='" + gid + "' ORDER BY cast(uses as unsigned) DESC LIMIT 99");
                rs = st.executeQuery("SELECT * FROM GuildStats WHERE GID='" + gid + "' ORDER BY cast(uses as unsigned) DESC LIMIT 99");
            } catch (Exception ignore) {
            }

            while (rs != null && rs.next()) {
                data.put(rs.getString("COMMAND"), Long.parseLong(rs.getString("USES")));
            }

        } catch (Exception ignore) {
        }
        return data;
    }

    public Long getStatsFromCommand(String command) {
        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM CommandStats WHERE COMMAND='" + command + "'");
                rs = st.executeQuery("SELECT * FROM CommandStats WHERE COMMAND='" + command + "'");
            } catch (Exception ignore) {
            }

            if (rs != null && rs.next()) {
                return Long.valueOf(rs.getString("USES"));
            }

        } catch (Exception ignore) {
        }
        return 1L;
    }

    public long getCommandStatsFromGuild(String gid, String command) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM GuildStats WHERE GID='" + gid + "' AND COMMAND='" + command + "'");
                rs = st.executeQuery("SELECT * FROM GuildStats WHERE GID='" + gid + "' AND COMMAND='" + command + "'");
            } catch (Exception ignore) {
            }

            if (rs != null && rs.next()) {
                return Long.parseLong(rs.getString("USES"));
            }

        } catch (Exception ignore) {
        }
        return 0L;
    }

    public HashMap<String, Long> getStatsForCommands() {

        HashMap<String, Long> data = new HashMap<>();

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM CommandStats ORDER BY cast(uses as unsigned) DESC LIMIT 99");
                rs = st.executeQuery("SELECT * FROM CommandStats ORDER BY cast(uses as unsigned) DESC LIMIT 99");
            } catch (Exception ignore) {
            }

            while (rs != null && rs.next()) {
                data.put(rs.getString("COMMAND"), Long.parseLong(rs.getString("USES")));
            }

        } catch (Exception ignore) {
        }
        return data;
    }

    public void addStats(Command cmd, String gid) {

        if (hasStatsGuild(gid, cmd.getCmd())) {
            Main.sqlConnector.query("UPDATE GuildStats SET USES='" + (getCommandStatsFromGuild(gid, cmd.getCmd()) + 1) + "' WHERE GID='" + gid + "' AND COMMAND='" + cmd.getCmd() + "'");
        } else {
            Main.sqlConnector.query("INSERT INTO GuildStats (GID, COMMAND, USES) VALUES ('" + gid + "', '" + cmd.getCmd() + "', '" + 1 + "');");
        }

        if (hasStatsCommand(cmd.getCmd())) {
            Main.sqlConnector.query("UPDATE CommandStats SET USES='" + (getStatsFromCommand(cmd.getCmd()) + 1) + "' WHERE COMMAND='" + cmd.getCmd() + "'");
        } else {
            Main.sqlConnector.query("INSERT INTO CommandStats (COMMAND, USES) VALUES ('" + cmd.getCmd() + "', '1');");
        }
    }

    //Setting-System

    public boolean hasSetting(String gid, String settingName) {

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Settings WHERE GID='" + gid + "' AND NAME='" + settingName + "'");
                rs = st.executeQuery("SELECT * FROM Settings WHERE GID='" + gid + "' AND NAME='" + settingName + "'");
            } catch (Exception ignore) {
            }

            return rs != null && rs.next();

        } catch (Exception ignore) {
        }

        createSettings(gid);

        return false;
    }

    public void setSetting(String gid, String settingName, boolean value) {

        if (hasSetting(gid, settingName))
            Main.sqlConnector.query("UPDATE Settings SET VALUE='" + value + "' WHERE GID='" + gid + "' AND NAME='" + settingName + "'");
        else
            Main.sqlConnector.query("INSERT INTO Settings (GID, NAME, VALUE) VALUES ('" + gid + "', '" + settingName + "', '" + value + "');");
    }

    public Boolean getSetting(String gid, String settingName) {
        boolean value = false;

        if (!hasSetting(gid, settingName)) return true;

        try {
            PreparedStatement st;
            ResultSet rs = null;

            try {
                st = Main.sqlConnector.con.prepareStatement("SELECT * FROM Settings WHERE GID='" + gid + "' AND NAME='" + settingName + "'");
                rs = st.executeQuery("SELECT * FROM Settings WHERE GID='" + gid + "' AND NAME='" + settingName + "'");
            } catch (Exception ignore) {}

            if (rs != null && rs.next())
                value = Boolean.parseBoolean(rs.getString("VALUE"));

        } catch (Exception ignore) {}

        return value;
    }

    public void createSettings(String gid) {
        for (Command commands : Main.commandManager.getCommands()) {
            if (commands.getCategory() == Category.HIDDEN) continue;
            if (!hasSetting(gid, "command_" + commands.getCmd().toLowerCase())) setSetting(gid, "command_" + commands.getCmd().toLowerCase(), true);
        }

        if (!hasSetting(gid, "logging_invite")) setSetting(gid, "logging_invite", true);
        if (!hasSetting(gid, "logging_memberjoin")) setSetting(gid, "logging_memberjoin", true);
        if (!hasSetting(gid, "logging_memberleave")) setSetting(gid, "logging_memberleave", true);
        if (!hasSetting(gid, "logging_memberban")) setSetting(gid, "logging_memberban", true);
        if (!hasSetting(gid, "logging_memberunban")) setSetting(gid, "logging_memberunban", true);
        if (!hasSetting(gid, "logging_nickname")) setSetting(gid, "logging_nickname", true);
        if (!hasSetting(gid, "logging_voicejoin")) setSetting(gid, "logging_voicejoin", true);
        if (!hasSetting(gid, "logging_voicemove")) setSetting(gid, "logging_voicemove", true);
        if (!hasSetting(gid, "logging_voiceleave")) setSetting(gid, "logging_voiceleave", true);
        if (!hasSetting(gid, "logging_roleadd")) setSetting(gid, "logging_roleadd", true);
        if (!hasSetting(gid, "logging_roleremove")) setSetting(gid, "logging_roleremove", true);
        if (!hasSetting(gid, "logging_voicechannel")) setSetting(gid, "logging_voicechannel", true);
        if (!hasSetting(gid, "logging_textchannel")) setSetting(gid, "logging_textchannel", true);
        if (!hasSetting(gid, "logging_rolecreate")) setSetting(gid, "logging_rolecreate", true);
        if (!hasSetting(gid, "logging_roledelete")) setSetting(gid, "logging_roledelete", true);
        if (!hasSetting(gid, "logging_rolename")) setSetting(gid, "logging_rolename", true);
        if (!hasSetting(gid, "logging_rolemention")) setSetting(gid, "logging_rolemention", true);
        if (!hasSetting(gid, "logging_rolehoisted")) setSetting(gid, "logging_rolehoisted", true);
        if (!hasSetting(gid, "logging_rolepermission")) setSetting(gid, "logging_rolepermission", true);
        if (!hasSetting(gid, "logging_rolecolor")) setSetting(gid, "logging_rolecolor", true);
        if (!hasSetting(gid, "logging_messagedelete")) setSetting(gid, "logging_messagedelete", true);
    }
}